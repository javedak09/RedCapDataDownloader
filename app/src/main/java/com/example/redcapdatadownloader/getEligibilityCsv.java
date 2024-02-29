package com.example.redcapdatadownloader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class getEligibilityCsv extends AsyncTask<String, String, String> {

    private static final Object APP_NAME = "GSED";
    private final String TAG = "GetPrePop()";
    private final Context mContext;
    HttpURLConnection urlConnection;
    private URL serverURL = null;
    private ProgressDialog pd;

    public getEligibilityCsv(Context context) {
        mContext = context;
    }

    public getEligibilityCsv(Context context, URL url) {
        mContext = context;
        serverURL = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContext);
        pd.setTitle("Syncing Prepop CSV");
        pd.setMessage("Getting connected to server...");
        pd.show();
        Log.d(TAG, "onPreExecute: Starting");
    }

    @Override
    protected String doInBackground(String... args) {
        Log.d(TAG, "doInBackground: Starting");
        StringBuilder result = new StringBuilder();

        URL url = null;
        try {
            Log.d(TAG, "doInBackground: Trying...");
            if (serverURL == null) {
                //    url = new URL("http://43.245.131.159:8080/dss/api/getdata.php");
                url = new URL("https://pedres2.aku.edu/GSEDapi/export_records_gsed.php");
            } else {
                url = serverURL;
            }
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(100000 /* milliseconds */);
            urlConnection.setConnectTimeout(150000 /* milliseconds */);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setUseCaches(false);
            urlConnection.connect();

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            JSONObject json = new JSONObject();
            try {
                json.put("table", "csv_prepop");
                json.put("filter", "status = ''");
                Log.d(TAG, "json.put: Done");
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.d(TAG, e1.getMessage());
            }
            Log.d(TAG, "downloadUrl: " + json.toString());
            wr.writeBytes(json.toString());
            wr.flush();
            wr.close();
            Log.d(TAG, "doInBackground: " + urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    Log.i(TAG, "ELIgi In: " + line);
                    result.append(line);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());

            return null;
        } finally {
//            urlConnection.disconnect();
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: Starting: " + result);

        //Do something with the JSON string
        if (result != null) {
            String json = result;
            if (json.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    CSVWriter writer = null;
                    //String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/childlist.csv"); // Here csv file name is MyCsvFile.csv
//                    sdDir = new File("/storage/emulated/0/com/forms/");
                    //File csvFolder = new File("/storage/emulated/0/com/forms/");

                    String[] folders = {
                            "GSED LF MINE-media",
                            "GSED SF MINE-media"
                    };
                    //File csvFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + APP_NAME);

                    for (String folder : folders) {
                        File csvFolder = new File("/storage/emulated/0/com/forms/" + folder);

                        if (!csvFolder.exists()) {
                            csvFolder.mkdirs();
                        }

                        File csvFile = new File(csvFolder + File.separator + "mine_enroll_info_csv.csv");
                        /*if (!csvFile.exists()) {
                            csvFolder.mkdirs();
                        }*/
                        writer = new CSVWriter(new FileWriter(csvFile));
                        List<String[]> data = new ArrayList<String[]>();
                    /*
                    // SAMPLE DATA FROM SERVER
                                        {
                                                "gsedid": "0081",
                                                "child_name": "FOKAIHA",
                                                "mother_name": "SABIRA",
                                                "dob": "19-05-2004"
                                        },
                    */


                        /*data.add(new String[]{
                                "parent_study_id_key",
                                "child_name",
                                "mother_name",
                                "father_name",
                                "child_sex",
                                "child_dob",
                                "child_ga",
                                "child_edd"
                        });
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObjectCC = jsonArray.getJSONObject(i);
                            data.add(new String[]{
                                    jsonObjectCC.getString("parent_study_id_key").equals("null") ? "" : jsonObjectCC.getString("parent_study_id_key"),
                                    jsonObjectCC.getString("child_name").equals("null") ? "" : jsonObjectCC.getString("child_name"),
                                    jsonObjectCC.getString("mother_name").equals("null") ? "" : jsonObjectCC.getString("mother_name"),
                                    jsonObjectCC.getString("father_name").equals("null") ? "" : jsonObjectCC.getString("father_name"),
                                    jsonObjectCC.getString("child_sex").equals("null") ? "" : jsonObjectCC.getString("child_sex"),
                                    jsonObjectCC.getString("child_ga").equals("null") ? "" : jsonObjectCC.getString("child_ga"),
                                    jsonObjectCC.getString("child_ga").equals("null") ? "" : jsonObjectCC.getString("child_ga"),
                                    jsonObjectCC.getString("child_edd").equals("null") ? "" : jsonObjectCC.getString("child_edd"),


                            });

                        }*/
                        writer.writeAll(data); // data is adding to csv

                        writer.close();
                    }
                    //callRead();
                    pd.setTitle("Success");
                    pd.setMessage("CSV File saved successfully.");
                    pd.show();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    pd.setTitle("Error");
                    pd.setMessage(e.getMessage());
                    pd.show();
                }
            } else {
                pd.setMessage("Received: " + json.length() + "");
                pd.show();
            }
        } else {
            pd.setTitle("Connection Error");
            pd.setMessage("Server not found!");
            pd.show();
        }
    }

}
