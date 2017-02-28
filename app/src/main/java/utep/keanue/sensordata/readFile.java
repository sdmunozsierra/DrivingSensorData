package utep.keanue.sensordata;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class readFile extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_file);

        TextView sensorData = (TextView) findViewById(R.id.txt_SensorData);
        sensorData.setMovementMethod(new ScrollingMovementMethod());

        sensorData.setText(FileHelper.ReadFile(readFile.this));

    }
}
