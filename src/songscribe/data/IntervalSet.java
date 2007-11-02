/*
SongScribe song notation program
Copyright (C) 2006-2007 Csaba Kavai

This file is part of SongScribe.

SongScribe is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

SongScribe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Created on Jul 16, 2006
*/
package songscribe.data;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Csaba Kávai
 */
public class IntervalSet {
    private LinkedList<Interval> is = new LinkedList<Interval>();

    public Interval addInterval(int a, int b){
        return addInterval(a, b, null);
    }

    public Interval addInterval(int a, int b, String data){
        if(a>=b)return null;
        //determining the new interval
        Interval ai = findInterval(a);
        Interval bi = findInterval(b);
        Interval newInterval = new Interval(ai!=null ? ai.a : a, bi!=null ? bi.b : b, data);

        //removing overlaps
        removeOverlaps(newInterval);

        //adding the new interval
        is.addFirst(newInterval);
        return newInterval;
    }

    public void removeInterval(int a, int b){
        Interval ai = findInterval(a);
        Interval bi = findInterval(b);
        if(ai!=null && ai==bi){
            bi = new Interval(bi.a, bi.b, bi.data);
            is.addFirst(bi);
        }
        if(ai!=null)ai.b=a;
        if(bi!=null)bi.a=b;

        //checking if any became zero-interval
        removeIfInvalid(ai);
        removeIfInvalid(bi);

        //removing overlaps
        removeOverlaps(a, b);
    }


    public ListIterator<Interval> listIterator(){
        return is.listIterator();
    }

    public Interval findInterval(int x){
        for(Interval i:is){
            if(i.a<=x && x<=i.b){
                return i;
            }
        }
        return null;
    }

    public void shiftValues(int from, int shift){
        for(ListIterator<Interval> li=is.listIterator();li.hasNext();){
            Interval i = li.next();
            if(i.a>=from)i.a+=shift;
            if(i.b>=from)i.b+=shift;
            if(i.a>=i.b)li.remove();
        }
    }

    public boolean isEmpty(){
        return is.isEmpty();
    }

    public IntervalSet copyInterval(int a, int b){
        IntervalSet retIs = new IntervalSet();
        for(Interval i:is){
            retIs.is.addFirst(new Interval(i.a, i.b, i.data));
        }
        retIs.removeInterval(Integer.MIN_VALUE, a);
        retIs.removeInterval(b, Integer.MAX_VALUE);
        return retIs;
    }

    private void removeIfInvalid(Interval interval) {
        if(interval!=null && interval.a>=interval.b)is.remove(interval);
    }

    private void removeOverlaps(Interval bigInterval) {
        removeOverlaps(bigInterval.a, bigInterval.b);
    }

    private void removeOverlaps(int a, int b) {
        for(ListIterator<Interval> it=is.listIterator();it.hasNext();){
            Interval i = it.next();
            if(a<=i.a && i.b<=b){
                it.remove();
            }
        }
    }

    public static void main(String[] args) throws java.io.IOException {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        IntervalSet is = new IntervalSet();
        while(true){
            System.out.print("Add(a) / Remove(r) / Quit(q): ");
            String str = br.readLine();
            if(str.equals("q")){
                System.exit(0);
            }
            System.out.print("a: ");
            int a = Integer.parseInt(br.readLine());
            System.out.print("b: ");
            int b = Integer.parseInt(br.readLine());
            if(str.equals("a")){
                is.addInterval(a, b);
            }else if(str.equals("r")){
                is.removeInterval(a, b);
            }

            for(Interval i:is.is){
                System.out.print("["+i.a+", "+i.b+"], ");
            }
            System.out.println();
        }
    }
}
