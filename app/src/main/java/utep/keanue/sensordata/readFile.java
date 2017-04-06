package utep.keanue.sensordata;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.File;

public class readFile extends MainActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_file);

        TextView sensorData = (TextView) findViewById(R.id.txt_SensorData);
        sensorData.setMovementMethod(new ScrollingMovementMethod());

        sensorData.setText(FileHelper.ReadFile(readFile.this));

        //Bottom Bar
        BottomBar bottomBarRead = (BottomBar) findViewById(R.id.bottomBar_read);
        bottomBarRead.selectTabWithId(R.id.tab_home);

        findViewById(R.id.tab_goBack).setOnClickListener(this);
        findViewById(R.id.tab_home).setOnClickListener(this);
        findViewById(R.id.tab_deleteFile).setOnClickListener(this);
    }
        @Override
        public void onClick(View v) {
            Class clazz = null;

            switch (v.getId()) {
                case R.id.tab_goBack:
                    finish();
                    break;
                case R.id.tab_home:
                    finish();
                    break;
                case R.id.tab_deleteFile:
                    File fileName = new File(FileHelper.path + FileHelper.fileName);
                    if (FileHelper.deleteFile(fileName)) {
                        Toast.makeText(readFile.this, "Deleted File", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(readFile.this, "Empty File", Toast.LENGTH_SHORT).show();
                    }
                    //Update view
                    readFile.this.recreate();
                    break;
            }
        }
    }
