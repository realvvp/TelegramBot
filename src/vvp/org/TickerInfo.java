/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vvp.org;

/**
 *
 * @author VVP
 */
public class TickerInfo {
    private double bid;
    private double ask;
    private double last;
    
    public TickerInfo(double bid, double ask, double last){
        this.bid = bid;
        this.ask = ask;
        this.last = last;
    }
    
    public double getBid(){
        return bid;
    }
    
    public double getAsk(){
        return ask;
    }
    
    public double getLask(){
        return last;
    }
}
