package utep.keanue.sensordata;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class readFile extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_file);

        TextView sensorData = (TextView) findViewById(R.id.txt_SensorData);
        sensorData.setMovementMethod(new ScrollingMovementMethod());

        sensorData.setText(FileHelper.ReadFile(readFile.this));

//        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar_read);
//        bottomBar.selectTabWithId(R.id.tab_readRecords);
//        //Bottom Bar On Select
//        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelected(@IdRes int tabId) {
//                //Read from file
//
//                if (tabId == R.id.tab_home){
//                    finish();
//                    return; //just in case
//                }
//                if (tabId == R.id.tab_startRecording){
//                    Toast.makeText(readFile.this, "STGART file", Toast.LENGTH_SHORT).show();
//
//                }
//                if (tabId == R.id.tab_deleteFile){
//                    Toast.makeText(readFile.this, "DELETE file", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        });
    }
}
