package group22.quikschedule;

import junit.framework.Assert;

import org.junit.Test;


import group22.quikschedule.Maps.Directions;

/**
 * Created by christoph on 11/26/16.
 */


public class ConvertAddress {

    @Test
    public void testConvertClassroom() {
        String address = "CENTR 115";
        String CenterHall = "Center Hall Library Walk, La Jolla, CA 92161 ";
        String converted = Directions.convertAddress(address);
        Assert.assertEquals(CenterHall, converted);
    }

    @Test
    public void testIgnoreZip() {
        String address = "San Diego 92122";
        String converted = Directions.convertAddress(address);
        Assert.assertEquals(converted, address);
    }

    @Test
    public void testIgnoreLongZip(){
        String address = "San Diego 92122-7154";
        String converted = Directions.convertAddress(address);
        Assert.assertEquals(converted, address);
    }

    @Test
    public void testConvertBoth() {
        String address = "WLH 92122";
        String result = "Warren Lecture Hall, La Jolla, CA 92161 92122";
        String converted = Directions.convertAddress(address);
        Assert.assertEquals(converted, result);
    }
}
