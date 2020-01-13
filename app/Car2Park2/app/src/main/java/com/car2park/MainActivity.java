package com.car2park;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private Matrix matrix;

    private ImageView map;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private static int INVALID_POINTER_ID = -1;
    private int firstPointerId = INVALID_POINTER_ID;
    private int secondPointerId = INVALID_POINTER_ID;

    private float firstPointerLastX, firstPointerLastY;
    private float secondPointerLastX, secondPointerLastY;

    private static int PARKING_SPACE_OCCUPIED = Color.parseColor("#5a5a5a");
    private static int PARKING_SPACE_FREE = Color.parseColor("#0080ff");

    private static int PARKING_SPACE_REFRESH_DELAY = 5000;

    private VectorDrawableCompat.VFullPath[] parkingSpaces =
            new VectorDrawableCompat.VFullPath[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        matrix = new Matrix();

        map = findViewById(R.id.iv_map);
        map.setScaleType(ImageView.ScaleType.MATRIX);
        map.setImageMatrix(matrix);

        gestureDetector = new GestureDetector(this, this);
        scaleGestureDetector = new ScaleGestureDetector(this, this);

        loadParkingSpaceVectors();

        setParkingSpaceOccupied(0);
        setParkingSpaceOccupied(1);
        setParkingSpaceOccupied(2);

        final Handler parkingSpaceRefreshHandler = new Handler();
        parkingSpaceRefreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadParkingSpaceStatus();
                parkingSpaceRefreshHandler.postDelayed(this,
                        PARKING_SPACE_REFRESH_DELAY);
            }
        }, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                firstPointerId = event.getPointerId(event.getActionIndex());

                firstPointerLastX = event.getX(event.findPointerIndex(firstPointerId));
                firstPointerLastY = event.getY(event.findPointerIndex(firstPointerId));
            } break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                secondPointerId = event.getPointerId(event.getActionIndex());

                secondPointerLastX = event.getX(event.findPointerIndex(secondPointerId));
                secondPointerLastY = event.getY(event.findPointerIndex(secondPointerId));
            } break;

            case MotionEvent.ACTION_UP:
                firstPointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondPointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL: {
                firstPointerId = INVALID_POINTER_ID;
                secondPointerId = INVALID_POINTER_ID;
            } break;

            case MotionEvent.ACTION_MOVE: {
                if (
                        firstPointerId != INVALID_POINTER_ID &&
                        secondPointerId != INVALID_POINTER_ID
                ) handleTwoPointerMove(event);
            } break;

        }

        return true;
    }

    private void handleTwoPointerMove(MotionEvent event) {
        // This is for rotating the map.
        // Rotation is kinda buggy but whatever.

        float firstPointerX = event.getX(event.findPointerIndex(firstPointerId));
        float firstPointerY = event.getY(event.findPointerIndex(firstPointerId));
        float secondPointerX = event.getX(event.findPointerIndex(secondPointerId));
        float secondPointerY = event.getY(event.findPointerIndex(secondPointerId));

        float initialAngle = calculateAngle(
                firstPointerLastX, firstPointerLastY,
                secondPointerLastX, secondPointerLastY
        );
        float currentAngle = calculateAngle(
                firstPointerX, firstPointerY,
                secondPointerX, secondPointerY
        );
        float angleDifferenceDegrees = (float) Math.toDegrees(
                currentAngle - initialAngle);

        float pivotX = (firstPointerX + secondPointerX) / 2.0f;
        float pivotY = (firstPointerY + secondPointerY) / 2.0f;

        matrix.postRotate(angleDifferenceDegrees, pivotX, pivotY);

        firstPointerLastX = firstPointerX;
        firstPointerLastY = firstPointerY;
        secondPointerLastX = secondPointerX;
        secondPointerLastY = secondPointerY;
    }

    private float calculateAngle(
            float firstPointerX, float firstPointerY,
            float secondPointerX, float secondPointerY
    ) {
        return (float) Math.atan2(
                firstPointerY - secondPointerY,
                firstPointerX - secondPointerX
        );
    }

    private void loadParkingSpaceVectors() {
        VectorChildFinder vectorChildFinder =
                new VectorChildFinder(this, R.drawable.map, map);

        parkingSpaces[0] = vectorChildFinder.findPathByName("parkingSpace0");
        parkingSpaces[1] = vectorChildFinder.findPathByName("parkingSpace1");
        parkingSpaces[2] = vectorChildFinder.findPathByName("parkingSpace2");
    }

    private void setParkingSpaceOccupied(int id) {
        if (parkingSpaces.length <= id || parkingSpaces[id] == null) return;
        parkingSpaces[id].setFillColor(PARKING_SPACE_OCCUPIED);
        parkingSpaces[id].setStrokeColor(PARKING_SPACE_OCCUPIED);
        map.postInvalidate();
    }

    private void setParkingSpaceFree(int id) {
        if (parkingSpaces.length <= id || parkingSpaces[id] == null) return;
        parkingSpaces[id].setFillColor(PARKING_SPACE_FREE);
        parkingSpaces[id].setStrokeColor(PARKING_SPACE_FREE);
        map.postInvalidate();
    }

    private void loadParkingSpaceStatus() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // use 10.0.2.2 to reach host from inside the emulator
        // TODO replace ip for integration
        String url = "http://10.0.2.2:5000/parking-spaces";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {

                        JSONObject parkingSpace = response.getJSONObject(i);

                        int id = parkingSpace.getInt("id");
                        boolean occupied = parkingSpace.getBoolean("occupied");

                        if (occupied) setParkingSpaceOccupied(id);
                        else setParkingSpaceFree(id);

                    } catch (JSONException e) {
                        Log.e(TAG, "error", e);
                    }
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "error", error);
            }

        });

        requestQueue.add(request);
    }

    /*
     * The following methods belong to GestureDetector.OnGestureListener.
     */

    @Override
    public boolean onDown(MotionEvent e) { return true; }

    @Override
    public void onShowPress(MotionEvent e) { }

    @Override
    public boolean onSingleTapUp(MotionEvent e) { return true; }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // This is for translating the map.
        matrix.postTranslate(-distanceX, -distanceY);
        map.setImageMatrix(matrix);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) { }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /*
     * The following methods belong to ScaleGestureDetector.OnScaleGestureListener.
     */

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // This is for scaling the map.
        matrix.postScale(
                detector.getScaleFactor(), detector.getScaleFactor(),
                detector.getFocusX(), detector.getFocusY()
        );
        map.setImageMatrix(matrix);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) { return true; }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) { }

}
