package com.example.campuscompanion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "campus_companion.db";
    private static final int DB_VERSION = 3;

    public static final String TABLE_STUDENTS = "students";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_ROLL = "roll";
    public static final String TABLE_ATTENDANCE = "attendance";
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_TIMETABLE = "timetable";

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, roll TEXT NOT NULL)");
        db.execSQL("CREATE TABLE attendance (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT NOT NULL, attended INTEGER NOT NULL, total INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, subject TEXT, due TEXT, due_millis INTEGER NOT NULL DEFAULT 0, completed INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE timetable (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT NOT NULL, day TEXT NOT NULL, start_time TEXT NOT NULL, room TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) db.execSQL("ALTER TABLE tasks ADD COLUMN due_millis INTEGER NOT NULL DEFAULT 0");
    }

    public long addStudent(String name, String roll) {
        ContentValues v=new ContentValues(); v.put(COL_NAME,name); v.put(COL_ROLL,roll); return getWritableDatabase().insert(TABLE_STUDENTS,null,v);
    }
    public void updateStudent(int id,String name,String roll){ ContentValues v=new ContentValues();v.put(COL_NAME,name);v.put(COL_ROLL,roll);getWritableDatabase().update(TABLE_STUDENTS,v,"id=?",new String[]{String.valueOf(id)}); }
    public void deleteStudent(int id){ getWritableDatabase().delete(TABLE_STUDENTS,"id=?",new String[]{String.valueOf(id)}); }

    public long addAttendance(String subject,int attended,int total){
        ContentValues v=new ContentValues();v.put("subject",subject);v.put("attended",attended);v.put("total",total);return getWritableDatabase().insert(TABLE_ATTENDANCE,null,v);
    }
    public void updateAttendance(int id,String subject,int attended,int total){ContentValues v=new ContentValues();v.put("subject",subject);v.put("attended",attended);v.put("total",total);getWritableDatabase().update(TABLE_ATTENDANCE,v,"id=?",new String[]{String.valueOf(id)});}
    public void deleteAttendance(int id){getWritableDatabase().delete(TABLE_ATTENDANCE,"id=?",new String[]{String.valueOf(id)});}
    public List<Attendance> getAttendanceList(){
        List<Attendance> list=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT id,subject,attended,total FROM attendance ORDER BY subject COLLATE NOCASE",null);
        while(c.moveToNext()) list.add(new Attendance(c.getInt(0),c.getString(1),c.getInt(2),c.getInt(3)));
        c.close(); return list;
    }
    public List<String> getAttendanceDetails(){
        List<String> list=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT subject,attended,total FROM attendance ORDER BY subject COLLATE NOCASE",null);
        while(c.moveToNext()){String s=c.getString(0);int a=c.getInt(1),t=c.getInt(2);double p=t>0?a*100.0/t:0;int need=lecturesNeeded(a,t);String advice=need>0?"Attend next "+need+" lecture"+(need==1?"":"s")+" to reach 75%":"At/above 75%";list.add(s+"\n"+a+" / "+t+" lectures • "+String.format(java.util.Locale.getDefault(),"%.1f%%",p)+"\n"+advice);}c.close();return list;
    }
    public static int lecturesNeeded(int attended,int total){if(total<=0)return 0; if(attended*100>=75*total)return 0; return (int)Math.ceil((75.0*total-100.0*attended)/25.0);}
    public static int lecturesCanMiss(int attended,int total){if(total<=0)return 0;if(attended*100<75*total)return 0;return (int)Math.floor((100.0*attended-75.0*total)/75.0);}
    public double getAttendancePercentage(){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(attended),0),COALESCE(SUM(total),0) FROM attendance",null);double p=0;if(c.moveToFirst()){int a=c.getInt(0),t=c.getInt(1);if(t>0)p=a*100.0/t;}c.close();return p;}

    public long addTask(String title,String subject,String due,long dueMillis){ContentValues v=new ContentValues();v.put("title",title);v.put("subject",subject);v.put("due",due);v.put("due_millis",dueMillis);return getWritableDatabase().insert(TABLE_TASKS,null,v);}
    public int getPendingTaskCount(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM tasks WHERE completed=0",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}
    public List<Task> getTasks(){List<Task> l=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,title,subject,due,due_millis,completed FROM tasks WHERE completed=0 ORDER BY CASE WHEN due_millis=0 THEN 1 ELSE 0 END,due_millis,id DESC",null);while(c.moveToNext())l.add(new Task(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getLong(4)));c.close();return l;}
    public List<String> getPendingTasks(int limit){List<String> l=new ArrayList<>();List<Task> t=getTasks();for(int i=0;i<t.size()&&i<limit;i++){Task x=t.get(i);l.add(x.title+(x.subject.isEmpty()?"":" • "+x.subject)+(x.due.isEmpty()?"":" • Due "+x.due));}return l;}
    public void completeTask(int id){ContentValues v=new ContentValues();v.put("completed",1);getWritableDatabase().update(TABLE_TASKS,v,"id=?",new String[]{String.valueOf(id)});}
    public void deleteTask(int id){getWritableDatabase().delete(TABLE_TASKS,"id=?",new String[]{String.valueOf(id)});}

    public long addTimetable(String subject,String day,String startTime,String room){ContentValues v=new ContentValues();v.put("subject",subject);v.put("day",day);v.put("start_time",startTime);v.put("room",room);return getWritableDatabase().insert(TABLE_TIMETABLE,null,v);}
    public void updateTimetable(int id,String subject,String day,String startTime,String room){ContentValues v=new ContentValues();v.put("subject",subject);v.put("day",day);v.put("start_time",startTime);v.put("room",room);getWritableDatabase().update(TABLE_TIMETABLE,v,"id=?",new String[]{String.valueOf(id)});}
    public void deleteTimetable(int id){getWritableDatabase().delete(TABLE_TIMETABLE,"id=?",new String[]{String.valueOf(id)});}

    private static final String[] DAY_ORDER={"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
    public List<Timetable> getTimetableList(){
        List<Timetable> list=new ArrayList<>();
        Cursor c=getReadableDatabase().rawQuery("SELECT id,day,subject,start_time,room FROM timetable",null);
        while(c.moveToNext()) list.add(new Timetable(c.getInt(0),c.getString(2),c.getString(1),c.getString(3),c.getString(4)));
        c.close();
        java.util.Collections.sort(list,(x,y)->{
            int dx=dayIndex(x.day),dy=dayIndex(y.day);
            if(dx!=dy) return Integer.compare(dx,dy);
            return x.startTime==null?0:x.startTime.compareToIgnoreCase(y.startTime==null?"":y.startTime);
        });
        return list;
    }
    private static int dayIndex(String day){
        if(day==null) return DAY_ORDER.length;
        for(int i=0;i<DAY_ORDER.length;i++) if(DAY_ORDER[i].equalsIgnoreCase(day.trim())) return i;
        return DAY_ORDER.length;
    }
    public static String capitalize(String s){return (s==null||s.isEmpty())?"":Character.toUpperCase(s.charAt(0))+s.substring(1);}

    public String getNextClass(){String day=new java.text.SimpleDateFormat("EEEE",java.util.Locale.getDefault()).format(new java.util.Date());Cursor c=getReadableDatabase().rawQuery("SELECT subject,start_time,room FROM timetable WHERE lower(day)=lower(?) ORDER BY start_time ASC LIMIT 1",new String[]{day});String r="No class added for today";if(c.moveToFirst())r=c.getString(0)+" • "+c.getString(1)+(c.getString(2)==null||c.getString(2).isEmpty()?"":" • "+c.getString(2));c.close();return r;}

    public List<Student> getAllStudents(){List<Student> l=new ArrayList<>();Cursor c=getReadableDatabase().query(TABLE_STUDENTS,null,null,null,null,null,"name ASC");while(c.moveToNext())l.add(new Student(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("name")),c.getString(c.getColumnIndexOrThrow("roll"))));c.close();return l;}

    public static class Task { public final int id; public final String title,subject,due; public final long dueMillis; public Task(int id,String title,String subject,String due,long dueMillis){this.id=id;this.title=title;this.subject=subject==null?"":subject;this.due=due==null?"":due;this.dueMillis=dueMillis;} }
    public static class Attendance { public final int id; public final String subject; public final int attended,total; public Attendance(int id,String subject,int attended,int total){this.id=id;this.subject=subject;this.attended=attended;this.total=total;} }
    public static class Timetable { public final int id; public final String subject,day,startTime,room; public Timetable(int id,String subject,String day,String startTime,String room){this.id=id;this.subject=subject;this.day=day;this.startTime=startTime;this.room=room==null?"":room;} }
}
