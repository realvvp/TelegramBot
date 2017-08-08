/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vvp.org;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author VVP
 */
public class CurrencyUpdater implements Runnable {

    ArrayList<String> currencies;
    
    private void initCurrencies(){
        currencies = new ArrayList<String>();
        
        currencies.add("btc");
        currencies.add("ltc");
        currencies.add("eth");
        currencies.add("dash");
    }
    
    @Override
    public void run() {
        initCurrencies();
        JsonObject rates = null;
        while (true){
            
            for(String curr : currencies){
                rates = performBasicRequest(MyTeleBot.API_URL.concat( curr ));
                if(rates != null){
                    if(rates.get("success").getAsBoolean()){
                        JsonObject ret = rates.getAsJsonObject("result");
                        if(ret != null){
                            synchronized (Map.class){
                                MyTeleBot.ratesValue.put( curr, 
                                        new TickerInfo(ret.get( "Bid" ).getAsDouble(), 
                                                ret.get( "Ask" ).getAsDouble(), 
                                                ret.get( "Last" ).getAsDouble()) );
                            }
                        }
                    }                          
                }
            }
            
            try {
                Thread.sleep(MyTeleBot.updatePeriod);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    protected JsonObject performBasicRequest(String currency) {
        JsonObject result = null;

        String URL = currency;

        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(URL);

        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();

            if (entity == null) {
                throw new NullPointerException();
            }

            result = new JsonParser().parse(IOUtils.toString(entity.getContent(), "UTF-8")).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }

        return result;
    }
    
}
