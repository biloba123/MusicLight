package com.lvqingyang.musiclight;

import java.util.List;

/**
 * 一句话功能描述
 * 功能详细描述
 *
 * @author Lv Qingyang
 * @date 2017/9/19
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 */
public class Command {
    private int op;//0: off or on, 1: led
    private int sw;//0: off, 1: on
    private List<LedItem> leds;
    private int cou;

    public Command(int op, int sw, List<LedItem> leds, int itemCount) {
        this.op = op;
        this.sw = sw;
        this.leds = leds;
        this.cou = itemCount;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public int getSw() {
        return sw;
    }

    public void setSw(int sw) {
        this.sw = sw;
    }

    public List<LedItem> getLeds() {
        return leds;
    }

    public void setLeds(List<LedItem> leds) {
        this.leds = leds;
    }

    public int getCou() {
        return cou;
    }

    public void setCou(int cou) {
        this.cou = cou;
    }
}
