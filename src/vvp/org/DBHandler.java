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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author User
 */
public class DBHandler {

    protected static Connection conn;
    protected static Statement statmt;
    protected static ResultSet resSet;

    private static DBHandler instance;

    public static synchronized DBHandler getInstance() {
        if ( instance == null ) {
            instance = new DBHandler();
        }

        return instance;
    }

    protected DBHandler() {
        doConnect();
    }

    protected void doConnect() {
        try {
            conn = null;
            Class.forName( "org.sqlite.JDBC" );
            try {
                Properties properties = new Properties();
                properties.setProperty( "PRAGMA foreign_keys", "ON" );
                conn = DriverManager.getConnection( "jdbc:sqlite:" + getDBPath(), properties );
            } catch ( SQLException ex ) {
                Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
            }

            System.out.println( "База Подключена!" );

            try {
                statmt = conn.createStatement();
                statmt.execute( "PRAGMA foreign_keys = ON" );
            } catch ( SQLException ex ) {
                Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
            }
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }
    
    private String getDBPath(){
        String result = null;
        
        FileReader fr;
        
        try {
            fr = new FileReader( "conf.json" );
            
            JsonObject obj = new JsonParser().parse( fr ).getAsJsonObject();
            
            if(obj.has( "DBPath" )){
                result = obj.get( "DBPath" ).getAsString();
            }
            
            fr.close();
        } catch ( FileNotFoundException ex ) {
            Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
        } catch ( IOException ex ) {
            Logger.getLogger( MyTeleBot.class.getName() ).log( Level.SEVERE, null, ex );
        }
        
        return result;
    }

    public ResultSet executeQuery( String query ) {
        resSet = null;

        synchronized (Statement.class){
            if ( conn != null && statmt != null && query != null ) {
                try {
                    resSet = statmt.executeQuery( query );
                } catch ( SQLException ex ) {
                    Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
                }
            }
        }

        return resSet;
    }

    public void executeUpdate( String query ) {
        synchronized (Statement.class){
            if ( conn != null && statmt != null && query != null ) {
                try {
                    statmt.executeUpdate( query );
                } catch ( SQLException ex ) {
                    Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
                }
            }
        }
    }

    public Boolean executeSaveUpdate( ArrayList<String> querys ) {
        Boolean ret = true;

        synchronized (Statement.class){
            try {
                statmt.executeUpdate( "BEGIN" );
                for ( int i = 0; i < querys.size(); i++ ) {
                    statmt.executeUpdate( querys.get( i ) );
                }
            } catch ( SQLException ex ) {
                try {
                    ret = false;
                    statmt.executeUpdate( "ROLLBACK" );
                    Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
                } catch ( SQLException ex1 ) {
                    ret = false;
                    Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex1 );
                }
            }

            if ( ret ) {
                try {
                    statmt.executeUpdate( "COMMIT" );
                } catch ( SQLException ex ) {
                    Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
                    ret = false;
                }
            }
        
        }

        return ret;
    }

    public void close() {
        try {
            conn.close();
        } catch ( SQLException ex ) {
            Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
        }
        try {
            resSet.close();
        } catch ( SQLException ex ) {
            Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
        }
        try {
            statmt.close();
        } catch ( SQLException ex ) {
            Logger.getLogger( DBHandler.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

}
