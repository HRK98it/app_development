package com.example.campuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.campuscompanion.db.DatabaseHelper;

public class HomeFragment extends Fragment {
    private DatabaseHelper db;
    private TextView tvAttendance,tvTasks,tvNextClass,tvTaskList;
    @Nullable public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){ return inflater.inflate(R.layout.fragment_home,container,false); }
    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle b){
        super.onViewCreated(v,b); db=new DatabaseHelper(requireContext());
        tvAttendance=v.findViewById(R.id.tvAttendance); tvTasks=v.findViewById(R.id.tvTasks); tvNextClass=v.findViewById(R.id.tvNextClass); tvTaskList=v.findViewById(R.id.tvTaskList);
        SwitchMaterial sw=v.findViewById(R.id.switchDarkMode); boolean dark=requireContext().getSharedPreferences("settings",0).getBoolean("dark",false); sw.setChecked(dark);
        sw.setOnCheckedChangeListener((button,checked)->{ requireContext().getSharedPreferences("settings",0).edit().putBoolean("dark",checked).apply(); AppCompatDelegate.setDefaultNightMode(checked?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_NO); });
        v.findViewById(R.id.actionAcademics).setOnClickListener(x->((MainActivity)requireActivity()).selectTab(R.id.nav_records));
        v.findViewById(R.id.actionWeather).setOnClickListener(x->((MainActivity)requireActivity()).selectTab(R.id.nav_weather));
        v.findViewById(R.id.actionCampus).setOnClickListener(x->((MainActivity)requireActivity()).selectTab(R.id.nav_map));
        v.findViewById(R.id.actionFocus).setOnClickListener(x->{ requireActivity().startService(new Intent(requireContext(),MusicService.class)); Toast.makeText(requireContext(),"Focus Mode music started",Toast.LENGTH_SHORT).show(); });
        refresh();
    }
    @Override public void onResume(){ super.onResume(); if(db!=null) refresh(); }
    private void refresh(){ double pct=db.getAttendancePercentage(); tvAttendance.setText(pct==0?"No attendance data":String.format(java.util.Locale.getDefault(),"%.1f%%",pct)); int n=db.getPendingTaskCount(); tvTasks.setText(String.valueOf(n)); tvNextClass.setText(db.getNextClass()); java.util.List<String> tasks=db.getPendingTasks(3); if(tasks.isEmpty()) tvTaskList.setText("No pending tasks. Add one from Academics."); else { StringBuilder s=new StringBuilder(); for(String t:tasks)s.append("• ").append(t).append("\n"); tvTaskList.setText(s.toString().trim()); }}
}
