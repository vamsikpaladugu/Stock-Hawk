package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.loopj.android.http.AsyncHttpClient.log;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailFragment extends Fragment {

    LineChart chart;
    private Spinner spDuration;

    List<String> xMonth;

    View view;

    String[] xValues = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public StockDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_stockdetail, container, false);

        spDuration = (Spinner) view.findViewById(R.id.spDuration);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_duration_textview, getResources().getStringArray(R.array.time_spane));
        spDuration.setAdapter(adapter);

        spDuration.setSelection(3);

        spDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                switch (pos) {
                    case 0:
                        getData(Calendar.YEAR, -1);
                        break;

                    case 1:
                        getData(Calendar.MONTH, -6);
                        break;

                    case 2:
                        getData(Calendar.MONTH, -3);
                        break;

                    case 3:
                        getData(Calendar.MONTH, -1);
                        break;


                    default:
                        getData(Calendar.YEAR, -1);
                        break;


                }


            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }


    private void getData(int field, int value) {

        Calendar calEnd = Calendar.getInstance();

        String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
        String SYMBOL = "\"" + getActivity().getIntent().getExtras().getString("symb") + "\"";



        String END_DATE = "\"" + calEnd.get(Calendar.YEAR) + "-" +formatMonth(calEnd.get(Calendar.MONTH))+ "-" + calEnd.get(Calendar.DAY_OF_MONTH) + "\"";

        Calendar calStart = Calendar.getInstance();
        calStart.add(field, value);

        String START_DATE = "\"" + calStart.get(Calendar.YEAR) + "-" + formatMonth(calStart.get(Calendar.MONTH)) + "-" + calStart.get(Calendar.DAY_OF_MONTH) + "\"";

        String YQL_STATEMENT = "select * from yahoo.finance.historicaldata where symbol =" + SYMBOL + "  and startDate =" + START_DATE + "  and endDate = " + END_DATE;
        String END_URL = "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";


        String url = BASE_URL + YQL_STATEMENT + END_URL;

        log.v("url", url.replace(" ", "%20"));


        Log.v("Start", "" + START_DATE);

        Log.v("end", "" + END_DATE);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url.replace(" ", "%20"), new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {


                try {

                    JSONObject object = new JSONObject(new String(responseBody));

                    Log.v("outp", object.toString());

                    drawChart(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });


    }

    private String formatMonth(int i) {

        i=i+1;

        if (i<10)
            return "0"+i;
        else
            return ""+i;
    }


    public void drawChart(JSONObject jsonObject) throws JSONException {

        chart = (LineChart) view.findViewById(R.id.chart);

        JSONArray jsonArray = jsonObject.getJSONObject("query").getJSONObject("results").getJSONArray("quote");


        xMonth = new ArrayList<>();

        List<Entry> entries = new ArrayList<Entry>();


        Log.v("json", jsonArray.toString());

        xMonth.clear();

        entries.clear();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject dayObject = jsonArray.getJSONObject((jsonArray.length() - i - 1));

            String[] date = dayObject.getString("Date").split("-");

            xMonth.add(date[2] + "," + xValues[Integer.parseInt(date[1]) - 1]);

            // turn your data into Entry objects
            entries.add(new Entry(i, (float) Double.parseDouble(dayObject.getString("Close"))));
        }

        Log.v("entries", "" + entries.size());

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(Color.RED);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.invalidate();

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                Log.v("", "" + value);

                if (((int) value) < xMonth.size())

                    return xMonth.get((int) value);

                else
                    return "";

            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        Log.v("Max", "" + chart.getYMax());
        Log.v("Min", "" + chart.getYMin());


        YAxis ryAxis = chart.getAxisRight();
        ryAxis.setEnabled(false);
        ryAxis.setDrawGridLines(false);

        YAxis lyAxis = chart.getAxisLeft();
        lyAxis.setEnabled(true);
        lyAxis.setDrawGridLines(false);

        lyAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "$ " + String.format("%.2f", value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });


        chart.setDescription("Max: $" + chart.getYMax() + "," + "Min: $" + chart.getYMin());

        //chart.setContentDescription("price variation chart Max: $"+chart.getYMax()+","+"Min: $"+chart.getYMin());


    }


}
