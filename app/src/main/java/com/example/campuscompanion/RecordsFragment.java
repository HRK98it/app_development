package com.example.campuscompanion;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuscompanion.db.DatabaseHelper;
import com.example.campuscompanion.db.Student;
import com.example.campuscompanion.db.StudentAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordsFragment extends Fragment implements StudentAdapter.OnStudentActionListener {
    public static final String EXTRA_STUDENT_ID="extra_student_id", EXTRA_STUDENT_NAME="extra_student_name", EXTRA_STUDENT_ROLL="extra_student_roll";
    private RecyclerView rvStudents; private Button btnAddStudent; private DatabaseHelper dbHelper; private StudentAdapter adapter;
    private LinearLayout llAttendanceList, llTaskList, llTimetableList;

    @Nullable @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){return inflater.inflate(R.layout.fragment_records,container,false);}
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState); dbHelper=new DatabaseHelper(requireContext());
        rvStudents=view.findViewById(R.id.rvStudents); btnAddStudent=view.findViewById(R.id.btnAddStudent);
        llAttendanceList=view.findViewById(R.id.llAttendanceList); llTaskList=view.findViewById(R.id.llTaskList); llTimetableList=view.findViewById(R.id.llTimetableList);
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnAddStudent.setOnClickListener(v->startActivity(new Intent(requireContext(),AddEditStudentActivity.class)));
        view.findViewById(R.id.btnAddAttendance).setOnClickListener(v->showAttendanceDialog(null));
        view.findViewById(R.id.btnAddTask).setOnClickListener(v->showTaskDialog());
        view.findViewById(R.id.btnAddClass).setOnClickListener(v->showClassDialog(null));
    }
    @Override public void onResume(){super.onResume();if(dbHelper!=null){loadStudents();refreshAcademicData();}}
    private void loadStudents(){List<Student> students=dbHelper.getAllStudents();adapter=new StudentAdapter(students,this);rvStudents.setAdapter(adapter);}
    private EditText field(String hint){EditText e=new EditText(requireContext());e.setHint(hint);e.setPadding(20,10,20,10);return e;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(requireContext());l.setOrientation(LinearLayout.VERTICAL);l.setPadding(12,4,12,4);return l;}

    // ---------- Attendance ----------

    private void showAttendanceDialog(@Nullable DatabaseHelper.Attendance editing){
        LinearLayout box=vertical(); EditText sub=field("Subject (e.g. DBMS)"), a=field("Lectures attended"), t=field("Total lectures"); a.setInputType(2);t.setInputType(2);box.addView(sub);box.addView(a);box.addView(t);
        if(editing!=null){sub.setText(editing.subject);a.setText(String.valueOf(editing.attended));t.setText(String.valueOf(editing.total));}
        TextView help=new TextView(requireContext());help.setPadding(20,12,20,4);help.setText("Enter the lectures attended and total lectures. The app will calculate your attendance and how many lectures you need to attend to reach 75%.");box.addView(help);
        new AlertDialog.Builder(requireContext()).setTitle(editing==null?"Add Subject Attendance":"Edit Subject Attendance").setView(box).setPositiveButton("Save",(d,w)->{
            try{
                String subject=sub.getText().toString().trim();int av=Integer.parseInt(a.getText().toString()),tv=Integer.parseInt(t.getText().toString());
                if(subject.isEmpty()||av<0||tv<=0||av>tv)throw new Exception();
                if(editing==null) dbHelper.addAttendance(subject,av,tv); else dbHelper.updateAttendance(editing.id,subject,av,tv);
                int need=DatabaseHelper.lecturesNeeded(av,tv);int miss=DatabaseHelper.lecturesCanMiss(av,tv);
                String msg=String.format(Locale.getDefault(),"%s: %.1f%%\n%s",subject,av*100.0/tv,need>0?"Attend next "+need+" lecture(s) to reach 75%.":"You are already at/above 75%. You can miss up to "+miss+" lecture(s).");
                Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show();refreshAttendanceList();
            }catch(Exception e){Toast.makeText(requireContext(),"Enter valid attendance values.",Toast.LENGTH_SHORT).show();}
        }).setNegativeButton("Cancel",null).show();
    }

    private void refreshAttendanceList(){
        if(llAttendanceList==null)return; llAttendanceList.removeAllViews();
        List<DatabaseHelper.Attendance> list=dbHelper.getAttendanceList();
        if(list.isEmpty()){llAttendanceList.addView(emptyCard("No subjects added yet."));return;}
        for(DatabaseHelper.Attendance a:list){
            double p=a.total>0?a.attended*100.0/a.total:0;
            int need=DatabaseHelper.lecturesNeeded(a.attended,a.total);
            String advice=need>0?"Attend next "+need+" lecture"+(need==1?"":"s")+" to reach 75%":"At/above 75%";
            String text=a.subject+"\n"+a.attended+" / "+a.total+" lectures • "+String.format(Locale.getDefault(),"%.1f%%",p)+"\n"+advice;
            llAttendanceList.addView(actionCard(text,
                    "Edit",false,v->showAttendanceDialog(a),
                    "Delete",true,v->{dbHelper.deleteAttendance(a.id);Toast.makeText(requireContext(),"Attendance record deleted",Toast.LENGTH_SHORT).show();refreshAttendanceList();}));
        }
    }

    // ---------- Tasks ----------

    private void showTaskDialog(){
        LinearLayout box=vertical(); EditText title=field("Task / Assignment"), sub=field("Subject"), due=field("Deadline");box.addView(title);box.addView(sub);box.addView(due); due.setFocusable(false); due.setOnClickListener(v->pickDateTime(due));
        TextView reminder=new TextView(requireContext()); reminder.setPadding(20,12,20,4); reminder.setText("Reminder: choose a deadline, then select when you want the notification."); box.addView(reminder);
        final long[] dueMillis={0}; due.setOnClickListener(v->pickDateTime(due,dueMillis));
        new AlertDialog.Builder(requireContext()).setTitle("Add Assignment / Task").setView(box).setPositiveButton("Save",(d,w)->{String ti=title.getText().toString().trim();if(ti.isEmpty()){Toast.makeText(requireContext(),"Enter a task title",Toast.LENGTH_SHORT).show();return;}long millis=dueMillis[0];if(millis==0){Toast.makeText(requireContext(),"Please select a deadline",Toast.LENGTH_SHORT).show();return;}dbHelper.addTask(ti,sub.getText().toString().trim(),due.getText().toString().trim(),millis);showReminderChoice(ti,sub.getText().toString().trim(),millis);refreshTaskList();}).setNegativeButton("Cancel",null).show();
    }

    private void refreshTaskList(){
        if(llTaskList==null)return; llTaskList.removeAllViews();
        List<DatabaseHelper.Task> tasks=dbHelper.getTasks();
        if(tasks.isEmpty()){llTaskList.addView(emptyCard("No tasks added yet."));return;}
        for(DatabaseHelper.Task t:tasks){
            String text="• "+t.title+(t.subject.isEmpty()?"":" — "+t.subject)+(t.due.isEmpty()?"":"\nDeadline: "+t.due);
            llTaskList.addView(actionCard(text,
                    "Done",false,v->{dbHelper.completeTask(t.id);Toast.makeText(requireContext(),"Marked as done",Toast.LENGTH_SHORT).show();refreshTaskList();},
                    "Delete",true,v->{dbHelper.deleteTask(t.id);Toast.makeText(requireContext(),"Task deleted",Toast.LENGTH_SHORT).show();refreshTaskList();}));
        }
    }

    private void pickDateTime(EditText target){pickDateTime(target,new long[]{0});}
    private void pickDateTime(EditText target,long[] result){
        Calendar c=Calendar.getInstance(); new DatePickerDialog(requireContext(),(dp,y,m,day)->{new TimePickerDialog(requireContext(),(tp,h,min)->{Calendar chosen=Calendar.getInstance();chosen.set(y,m,day,h,min,0);chosen.set(Calendar.MILLISECOND,0);result[0]=chosen.getTimeInMillis();target.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(chosen.getTime()));},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),false).show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showReminderChoice(String title,String subject,long deadline){
        String[] options={"At deadline","1 hour before","1 day before"};
        new AlertDialog.Builder(requireContext()).setTitle("Set reminder").setItems(options,(d,which)->{long when=deadline-(which==1?60*60*1000L:which==2?24*60*60*1000L:0);if(when<=System.currentTimeMillis()){Toast.makeText(requireContext(),"That reminder time has already passed; task was saved without a future reminder.",Toast.LENGTH_LONG).show();return;}scheduleReminder(title,subject,when);Toast.makeText(requireContext(),"Reminder scheduled",Toast.LENGTH_SHORT).show();}).setNegativeButton("No reminder",null).show();
    }
    private void scheduleReminder(String title,String subject,long when){
        if(Build.VERSION.SDK_INT>=33 && ActivityCompat.checkSelfPermission(requireContext(),"android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},44);
        AlarmManager alarm=(AlarmManager)requireContext().getSystemService(android.content.Context.ALARM_SERVICE); if(Build.VERSION.SDK_INT>=31 && !alarm.canScheduleExactAlarms()){startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));return;}
        Intent i=new Intent(requireContext(),TaskReminderReceiver.class);i.putExtra("title",title);i.putExtra("subject",subject);android.app.PendingIntent pi=android.app.PendingIntent.getBroadcast(requireContext(),(int)(when%Integer.MAX_VALUE),i,android.app.PendingIntent.FLAG_UPDATE_CURRENT|android.app.PendingIntent.FLAG_IMMUTABLE);if(Build.VERSION.SDK_INT>=23)alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);else alarm.setExact(AlarmManager.RTC_WAKEUP,when,pi);
    }

    // ---------- Timetable ----------

    private void showClassDialog(@Nullable DatabaseHelper.Timetable editing){
        LinearLayout box=vertical();EditText sub=field("Subject"),day=field("Day (e.g. Monday)"),time=field("Start time (e.g. 10:30 AM)"),room=field("Room");box.addView(sub);box.addView(day);box.addView(time);box.addView(room);
        if(editing!=null){sub.setText(editing.subject);day.setText(editing.day);time.setText(editing.startTime);room.setText(editing.room);}
        new AlertDialog.Builder(requireContext()).setTitle(editing==null?"Add Timetable Class":"Edit Timetable Class").setView(box).setPositiveButton("Save",(d,w)->{
            String subject=sub.getText().toString().trim(),dayVal=day.getText().toString().trim();
            if(subject.isEmpty()||dayVal.isEmpty()){Toast.makeText(requireContext(),"Enter subject and day",Toast.LENGTH_SHORT).show();return;}
            if(editing==null) dbHelper.addTimetable(subject,dayVal,time.getText().toString().trim(),room.getText().toString().trim());
            else dbHelper.updateTimetable(editing.id,subject,dayVal,time.getText().toString().trim(),room.getText().toString().trim());
            Toast.makeText(requireContext(),"Class saved",Toast.LENGTH_SHORT).show();refreshTimetableList();
        }).setNegativeButton("Cancel",null).show();
    }

    private void refreshTimetableList(){
        if(llTimetableList==null)return; llTimetableList.removeAllViews();
        List<DatabaseHelper.Timetable> list=dbHelper.getTimetableList();
        if(list.isEmpty()){llTimetableList.addView(emptyCard("No classes added yet."));return;}
        for(DatabaseHelper.Timetable t:list){
            String room=t.room.isEmpty()?"":" • "+t.room;
            String text=DatabaseHelper.capitalize(t.day)+" — "+t.subject+" • "+t.startTime+room;
            llTimetableList.addView(actionCard(text,
                    "Edit",false,v->showClassDialog(t),
                    "Delete",true,v->{dbHelper.deleteTimetable(t.id);Toast.makeText(requireContext(),"Class deleted",Toast.LENGTH_SHORT).show();refreshTimetableList();}));
        }
    }

    // ---------- Shared list-row UI helpers ----------

    private void refreshAcademicData(){refreshAttendanceList();refreshTaskList();refreshTimetableList();}

    private TextView emptyCard(String message){
        TextView tv=new TextView(requireContext());tv.setText(message);tv.setTextColor(ContextCompat.getColor(requireContext(),R.color.text_secondary));tv.setTextSize(14);tv.setPadding(14,14,14,14);tv.setBackgroundResource(R.drawable.bg_card);return tv;
    }

    /** Builds one card row: details text on the left, up to two small action buttons on the right. */
    private MaterialCardView actionCard(String detailsText,String action1Label,boolean action1Danger,View.OnClickListener action1,
                                         String action2Label,boolean action2Danger,View.OnClickListener action2){
        MaterialCardView card=new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin=10; card.setLayoutParams(cardParams);
        card.setRadius(20); card.setCardElevation(1);
        card.setStrokeColor(ContextCompat.getColor(requireContext(),R.color.border)); card.setStrokeWidth(1);
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.surface));

        LinearLayout content=vertical(); content.setPadding(16,16,16,16);
        TextView tvDetails=new TextView(requireContext()); tvDetails.setText(detailsText); tvDetails.setTextSize(14); tvDetails.setTextColor(ContextCompat.getColor(requireContext(),R.color.text_primary));
        content.addView(tvDetails);

        LinearLayout actions=new LinearLayout(requireContext()); actions.setOrientation(LinearLayout.HORIZONTAL); actions.setGravity(Gravity.END);
        LinearLayout.LayoutParams actionsParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); actionsParams.topMargin=12; actions.setLayoutParams(actionsParams);
        actions.addView(actionButton(action1Label,action1Danger,action1));
        MaterialButton second=actionButton(action2Label,action2Danger,action2);
        ((LinearLayout.LayoutParams)second.getLayoutParams()).leftMargin=8;
        actions.addView(second);
        content.addView(actions);

        card.addView(content);
        return card;
    }

    private MaterialButton actionButton(String label,boolean danger,View.OnClickListener listener){
        MaterialButton b=new MaterialButton(requireContext());
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,110));
        b.setText(label); b.setTextSize(12); b.setTypeface(b.getTypeface(), Typeface.BOLD); b.setCornerRadius(12); b.setInsetTop(0); b.setInsetBottom(0);
        b.setMinWidth(0); b.setMinimumWidth(0);
        if(danger){ b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.danger)); b.setTextColor(ContextCompat.getColor(requireContext(),R.color.white)); }
        else { b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.surface)); b.setStrokeWidth(1); b.setStrokeColor(ContextCompat.getColorStateList(requireContext(),R.color.border)); b.setTextColor(ContextCompat.getColor(requireContext(),R.color.primary)); }
        b.setOnClickListener(listener);
        return b;
    }

    @Override public void onEdit(Student student){Intent i=new Intent(requireContext(),AddEditStudentActivity.class);i.putExtra(EXTRA_STUDENT_ID,student.getId());i.putExtra(EXTRA_STUDENT_NAME,student.getName());i.putExtra(EXTRA_STUDENT_ROLL,student.getRoll());startActivity(i);}
    @Override public void onDelete(Student student){dbHelper.deleteStudent(student.getId());loadStudents();}
}
