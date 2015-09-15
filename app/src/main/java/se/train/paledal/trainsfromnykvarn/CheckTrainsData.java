package se.train.paledal.trainsfromnykvarn;

import android.os.AsyncTask;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Erik on 2015-09-14.
 */
class CheckTrainsData extends AsyncTask<Void, Void, List<TrainData>> {
    private Exception exception;
    private List<TextView> departures = new ArrayList<TextView>();
    public CheckTrainsData(List<TextView> tvl)
    {
        this.departures = tvl;
    }

    @Override
    protected List<TrainData> doInBackground(Void... params) {
        try {
            String response = sendGet();
            return parseXml(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected void onPostExecute(List<TrainData> result)
    {
        for (int i=0;i<departures.size()/4 && i<3 && i<result.size();i++) {
            departures.get(4*i+0).setText(result.get(i).avgang);
            departures.get(4*i+1).setText(result.get(i).tagnr);
            departures.get(4*i+2).setText(result.get(i).Information);
            departures.get(4*i+3).setText(result.get(i).gissadAvgang);
        }
    }

    private String sendGet() throws Exception {
        String url = "http://api.trafikinfo.trafikverket.se/v1/data.xml";
        String request = "<REQUEST>" +
                "<LOGIN authenticationkey='fe898a06841443beb05ea563a1ff2b8c' />" +
                "<QUERY objecttype='TrainAnnouncement' orderby='AdvertisedTimeAtLocation'>" +
                "<FILTER>" +
                "<AND>" +
                "<EQ name='ActivityType' value='Avgang' />" +
                "<EQ name='LocationSignature' value='Nkv' />" +
                "<EQ name='ToLocation' value='Cst' />" +
                "<OR>" +
                "<AND>" +
                "<GT name='AdvertisedTimeAtLocation' value='$dateadd(-00:15:00)' />" +
                "<LT name='AdvertisedTimeAtLocation' value='$dateadd(15:00:00)' />" +
                "</AND>" +
                "<AND>" +
                    "<LT name='AdvertisedTimeAtLocation' value='$dateadd(00:30:00)' />" +
                    "<GT name='EstimatedTimeAtLocation' value='$dateadd(-00:15:00)' />" +
                "</AND>" +
                "</OR>" +
                "</AND>" +
                "</FILTER>" +
                "<INCLUDE>AdvertisedTrainIdent</INCLUDE>" +
                "<INCLUDE>AdvertisedTimeAtLocation</INCLUDE>" +
                "<INCLUDE>EstimatedTimeAtLocation</INCLUDE>" +
                "<INCLUDE>EstimatedTimeIsPreliminary</INCLUDE>" +
                "<INCLUDE>ToLocation</INCLUDE>" +
                "<INCLUDE>Canceled</INCLUDE>" +
                "</QUERY>" +
                "</REQUEST>";
        try {
            URL iurl = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) iurl.openConnection();
            uc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
            wr.writeBytes(request);
            wr.flush();
            wr.close();

            int responseCode = uc.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url + request);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(uc.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (MalformedURLException e) {
            return e.toString();
        }
    }

    private List<TrainData> parseXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("TrainAnnouncement");


        List<TrainData> tdl = new ArrayList<TrainData>();
        for (int i=0;i<nodes.getLength();i++)
        {
            Node node = nodes.item(i);
            Element element = (Element)node;
            String trainNo = element.getElementsByTagName("AdvertisedTrainIdent").item(0).getFirstChild().getNodeValue();
            String time = element.getElementsByTagName("AdvertisedTimeAtLocation").item(0).getFirstChild().getNodeValue();
            String estTime = "-";
            time = time.split("T")[1];
            time = time.split(":")[0] + ":" + time.split(":")[1];
            String info = "-";
            if (element.getElementsByTagName("EstimatedTimeAtLocation").getLength() > 0) {
                if (element.getElementsByTagName("EstimatedTimeAtLocation").item(0).getFirstChild().getNodeValue().toString() != element.getElementsByTagName("AdvertisedTimeAtLocation").item(0).getFirstChild().getNodeValue().toString()) {
                    info = "Delayed";
                    estTime = element.getElementsByTagName("EstimatedTimeAtLocation").item(0).getFirstChild().getNodeValue();
                    estTime = estTime.split("T")[1];
                    estTime = estTime.split(":")[0] + ":" + estTime.split(":")[1];
                }
            }
            if (element.getElementsByTagName("EstimatedTimeIsPreliminary").item(0).getFirstChild().getNodeValue().toString() == "true")
            {
                info = "Preliminary";
            }
            if (element.getElementsByTagName("Canceled").item(0).getFirstChild().getNodeValue().toString() == "true")
            {
                info = "Canceled";
            }
            TrainData td = new TrainData();
            td.tagnr = trainNo;
            td.avgang = time;
            td.Information = info;
            td.nastaStation ="-";
            td.gissadAvgang = estTime;
            tdl.add(td);
        }

        return tdl;
    }
}
