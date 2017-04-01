package utep.keanue.sensordata;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by xerg on 4/1/2017.
 */


public class readFragment extends Fragment {

    private TextView data;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.read_records, container, false);


        TextView sensorData = (TextView) getView().findViewById(R.id.txt_SensorData);
        sensorData.setMovementMethod(new ScrollingMovementMethod());

        sensorData.setText(FileHelper.ReadFile(getActivity()  ));





        return v;
    }



}
