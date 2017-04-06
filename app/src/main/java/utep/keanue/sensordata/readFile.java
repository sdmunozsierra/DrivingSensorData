package utep.keanue.sensordata;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.roughike.bottombar.BottomBar;
import java.io.File;

public class readFile extends MainActivity implements View.OnClickListener {

    BottomBar bottomBarRead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_file);

        TextView textSensorData = (TextView) findViewById(R.id.txt_SensorData);
        textSensorData.setMovementMethod(new ScrollingMovementMethod());
        textSensorData.setText(FileHelper.ReadFile(readFile.this));

        //Bottom Bar
        bottomBarRead = (BottomBar) findViewById(R.id.bottomBar_read);
        bottomBarRead.selectTabWithId(R.id.tab_read2);

        findViewById(R.id.tab_read2).setOnClickListener(this);
        findViewById(R.id.tab_home2).setOnClickListener(this);
        findViewById(R.id.tab_deleteFile).setOnClickListener(this);

        //TODO Feature: Dynamic Update

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_read2:
                break;
            case R.id.tab_home2:
                //Sweep 'Animation'
                bottomBar.selectTabAtPosition(2, true);
                bottomBarRead.selectTabWithId(R.id.tab_home2);
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
