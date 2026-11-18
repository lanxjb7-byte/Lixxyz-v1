
package com.lixxzy.v1;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.provider.MediaStore;
import android.view.Surface;
import android.widget.Toast;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CaptureService extends Service {

    public static final String ACTION_START = "com.lixxzy.v1.CAPTURE_START";
    public static final String ACTION_STOP = "com.lixxzy.v1.CAPTURE_STOP";

    private static final String CHANNEL = "lixxzy_capture";

    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private MediaRecorder recorder;
    private String mode = "screenshot";
    private File recordFile;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                CHANNEL,
                "Lixxzy Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            );

            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(c);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {

        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopRecording();
            return START_NOT_STICKY;
        }

        if (intent != null && ACTION_START.equals(intent.getAction())) {
            mode = intent.getStringExtra("mode");

            int result = intent.getIntExtra(
                "resultCode",
                Activity.RESULT_CANCELED
            );

            Intent data = intent.getParcelableExtra("data");

            startForeground(52, notification());
            beginCapture(result, data);
        }

        return START_NOT_STICKY;
    }

    private Notification notification() {
        Notification.Builder b =
            Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL)
            : new Notification.Builder(this);

        return b.setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle("Lixxzy V1")
            .setContentText("Screen capture sedang berjalan")
            .setOngoing(true)
            .build();
    }

    private void beginCapture(int result, Intent data) {
        try {
            MediaProjectionManager mgr =
                (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);

            projection = mgr.getMediaProjection(result, data);

            if (projection == null) {
                stopSelf();
                return;
            }

            projection.registerCallback(
                new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        stopSelf();
                    }
                },
                handler
            );

            int w = getResources().getDisplayMetrics().widthPixels;
            int h = getResources().getDisplayMetrics().heightPixels;
            int density = getResources().getDisplayMetrics().densityDpi;

            if ("record".equals(mode)) {
                startRecording(w, h, density);
            } else {
                takeScreenshot(w, h, density);
            }

        } catch (Exception e) {
            Toast.makeText(
                this,
                "Screen capture gagal: " + e.getMessage(),
                Toast.LENGTH_LONG
            ).show();

            stopSelf();
        }
    }

    private void takeScreenshot(int w, int h, int density) {

        reader = ImageReader.newInstance(
            w, h, PixelFormat.RGBA_8888, 2
        );

        display = projection.createVirtualDisplay(
            "LixxzyScreenshot",
            w,
            h,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.getSurface(),
            null,
            handler
        );

        handler.postDelayed(() -> {

            Image image = null;

            try {
                image = reader.acquireLatestImage();

                if (image == null) {
                    Toast.makeText(
                        this,
                        "Screenshot gagal, coba lagi.",
                        Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Image.Plane plane = image.getPlanes()[0];

                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int rowPadding = rowStride - pixelStride * w;

                Bitmap bitmap = Bitmap.createBitmap(
                    w + rowPadding / pixelStride,
                    h,
                    Bitmap.Config.ARGB_8888
                );

                bitmap.copyPixelsFromBuffer(plane.getBuffer());

                Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, w, h);

                saveBitmap(cropped);

                bitmap.recycle();
                cropped.recycle();

            } catch (Exception e) {
                Toast.makeText(
                    this,
                    "Gagal menyimpan screenshot.",
                    Toast.LENGTH_LONG
                ).show();

            } finally {
                if (image != null) image.close();
                stopCapture();
            }

        }, 900);
    }

    private void saveBitmap(Bitmap bitmap) throws Exception {

        String name = "Lixzzy_" +
            new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(new Date()) + ".png";

        if (Build.VERSION.SDK_INT >= 29) {

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Lixxzy");

            android.net.Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            );

            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }

        } else {

            File dir = new File(
                getExternalFilesDir(null),
                "Screenshots"
            );

            dir.mkdirs();

            File f = new File(dir, name);
            FileOutputStream out = new FileOutputStream(f);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        }

        Toast.makeText(
            this,
            "Screenshot tersimpan di Pictures/Lixxzy.",
            Toast.LENGTH_LONG
        ).show();
    }

    private void startRecording(int w, int h, int density) throws Exception {

        File dir = new File(
            getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "Lixzzy"
        );

        dir.mkdirs();

        recordFile = new File(
            dir,
            "Lixxzy_" + System.currentTimeMillis() + ".mp4"
        );

        recorder = new MediaRecorder();

        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(recordFile.getAbsolutePath());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoSize(w, h);
        recorder.setVideoFrameRate(30);
        recorder.setVideoEncodingBitRate(5000000);

        recorder.prepare();

        Surface surface = recorder.getSurface();

        display = projection.createVirtualDisplay(
            "LixxzyRecord",
            w,
            h,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            handler
        );

        recorder.start();

        Toast.makeText(
            this,
            "Recording dimulai.",
            Toast.LENGTH_LONG
        ).show();
    }

    private void stopRecording() {

        try {
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;

                Toast.makeText(
                    this,
                    "Video tersimpan: " + recordFile.getAbsolutePath(),
                    Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {
            Toast.makeText(
                this,
                "Rekaman terlalu singkat atau gagal disimpan.",
                Toast.LENGTH_LONG
            ).show();
        }

        stopCapture();
    }

    private void stopCapture() {

        try {
            if (display != null) {
                display.release();
                display = null;
            }
        } catch (Exception ignored) {}

        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (Exception ignored) {}

        try {
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
        } catch (Exception ignored) {}

        try {
            if (projection != null) {
                projection.stop();
                projection = null;
            }
        } catch (Exception ignored) {}

        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopCapture();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }
    }
        
