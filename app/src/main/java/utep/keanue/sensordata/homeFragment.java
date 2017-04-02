package utep.keanue.sensordata;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by xerg on 4/1/2017.
 */

public class homeFragment extends Fragment{

    TextView xText, yText, zText;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_content, container, false);
        //Assign Accelerometer
        xText = (TextView) v.findViewById(R.id.xText);
        yText = (TextView) v.findViewById(R.id.yText);
        zText = (TextView) v.findViewById(R.id.zText);



        return v;
    }



}
