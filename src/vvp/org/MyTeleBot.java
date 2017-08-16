/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vvp.org;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 *
 * @author vvp
 */
public class MyTeleBot extends TelegramLongPollingBot {

    //private static String BTCE_BASIC_URL = "https://btc-e.com/api/3/ticker/";
    protected static final String API_URL = "https://bittrex.com/api/v1.1/public/getticker?market=usdt-";
    
    protected static int updatePeriod = 5000;
    
    protected static Map<String, TickerInfo> ratesValue = new LinkedHashMap<String, TickerInfo>( 2, 0.75f, false );

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            MyTeleBot bot = new MyTeleBot();
            bot.loadPeriod();
            
            telegramBotsApi.registerBot(bot);
            
            CurrencyUpdater currencyUpdater = new CurrencyUpdater();
            Thread updater = new Thread(currencyUpdater);
            updater.setDaemon( true );
            
            updater.start();
            
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "btcETestBot"; // You will find it at t.me/btcETestBot
    }

    @Override
    public String getBotToken() {
        return "313569665:AAE4U0_apjyFljjjDEMmFyRs3z7dpxXd3s8";
        
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().equals("/btc")) {
                sendMsg(message, getCurrencyRates( "btc" ));
            } else if (message.getText().equals("/ltc")) {
                sendMsg(message, getCurrencyRates( "ltc" ));
            } else if (message.getText().equals("/eth")) {
                sendMsg(message, getCurrencyRates( "eth" ));
            } else if (message.getText().equals("/DASH")) {
                sendMsg(message, getCurrencyRates( "dash" ));
            } else if (message.getText().equals("/help")) {
                sendMsg(message, getHelpString());
            } else if (message.getText().equals("/hello")) {
                sendMsg(message, "Привет!");
            } else {
                sendMsg(message, "Я не знаю что ответить на это, пожалуйста, используйте команду /help для вызова справки");
            }
        }
    }
    
    private void loadPeriod(){
        FileReader fr;
        
        try {
            fr = new FileReader( "conf.json" );
            
            JsonObject obj = new JsonParser().parse( fr ).getAsJsonObject();
            
            if(obj.has( "updatePeriodMS" )){
                updatePeriod = obj.get( "updatePeriodMS" ).getAsInt();
            }
            
            fr.close();
        } catch ( FileNotFoundException ex ) {
            Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
        } catch ( IOException ex ) {
            Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        //sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    protected String getCurrencyRates(String currency) {
        String result = null;
        String rateName = null;

        if (currency != null) {
            rateName = currency.concat( "/usd" );
            
            synchronized (Map.class){
                if(ratesValue.containsKey( currency )){
                    TickerInfo ti = ratesValue.get(currency);
                    
                    result = "Курс " + rateName.toUpperCase() + ":\n" ;
                    result += "Покупка: " + ti.getBid() + "\n";
                    result += "Продажа: " + ti.getAsk() + "\n";
                }
            }
        }
        return result;
    }

    protected String getHelpString() {
        String result;

        result = "Я выполняю следующие команды: \n";
        result += "/hello - поздороваюсь с Вами\n";
        result += "/btc - покажу курс BTC/USD\n";
        result += "/ltc - покажу курс LTC/USD\n";
        result += "/eth - покажу курс ETH/USD\n";
        result += "/DASH - покажу курс DSH/USD\n";

        return result;
    }
}
