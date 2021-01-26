package com.example.plan4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class Home extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {



    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    int hourOfAlarm=-999,minuteOfAlarm=-999;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private String onlineUserID = mUser.getUid();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID);
    final  String authUserEmailAddress = mUser.getEmail();

    private ProgressDialog loader;

    private String key = "";
    private  String task;
    Button pickTime;
    LinearLayout ll;

    private TextView displayAlarmTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        floatingActionButton = findViewById(R.id.fab);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        loader.setMessage("Fetching Tasks");
        loader.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                loader.dismiss();
            }
        }, 2200);

        //mUser = mAuth.getCurrentUser();
        //onlineUserID = mUser.getUid();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });
    }


    private void addTask() {
        hourOfAlarm=-999;
        minuteOfAlarm=-999;
        AlertDialog.Builder myDialog= new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View myView = layoutInflater.inflate(R.layout.input_file,null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();

        final EditText task = myView.findViewById(R.id.task);
        Button add = myView.findViewById(R.id.saveButton);
        Button cancel = myView.findViewById(R.id.cancelButton);
        pickTime = myView.findViewById(R.id.pickTime);
        ll = (LinearLayout)myView.findViewById(R.id.holderView);


        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"Time Picker");
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mTask = task.getText().toString().trim();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if(TextUtils.isEmpty(mTask)) {
                    task.setError("Cannot add Empty task");
                    return;
                }
                else {
                    loader.setMessage("Adding Task");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Model model = new Model(mTask,id,date);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(Home.this,"Task Added to the List",Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(Home.this,"Failed" + error,Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });


                }
                dialog.dismiss();
                if(!(hourOfAlarm == -999 || minuteOfAlarm == -999 )) {
                    Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                    intent.putExtra(AlarmClock.EXTRA_HOUR,hourOfAlarm);
                    intent.putExtra(AlarmClock.EXTRA_MINUTES,minuteOfAlarm);
                    intent.putExtra(AlarmClock.EXTRA_MESSAGE,mTask);
                    startActivity(intent);
                }

            }
        });

    }
    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
        ll.removeAllViews();
        hourOfAlarm = hourOfDay;
        minuteOfAlarm = minuteOfDay;
        String time ="Selected time: " + hourOfDay + ":" + minuteOfDay;
        TextView displayTime = new TextView(this);
        displayTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        displayTime.setTextColor(Color.parseColor("#FFFFFF"));
        displayTime.setPadding(40,23,15,10);
        displayTime.setText(time);
        ll.addView(displayTime);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference,Model.class).build();

        FirebaseRecyclerAdapter<Model,MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrive_layout,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder {
        View mView;
        public  MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTask(String task) {
            TextView taskTextView = mView.findViewById(R.id.taskTv);
            taskTextView.setText(task);
        }

        public void setDate(String date) {
            TextView dateTextView = mView.findViewById(R.id.dateTv);
            String value = "Created on " + date;
            dateTextView.setText(value);
        }
    }

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data,null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();

        final EditText mTask = view.findViewById(R.id.mEditTextTask);

        mTask.setText(task);
        mTask.setSelection(task.length());

        Button deleteButton = view.findViewById(R.id.buttonDelete);
        Button updateButton = view.findViewById(R.id.buttonUpdate);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = mTask.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());

                Model model = new Model(task,key,date);
                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(Home.this,"Task Updated",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(Home.this,"Update Failed. Try again later" + error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Home.this,"Task Deleted",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(Home.this,"Deletion Failed! Try again later" + error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(Home.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.changePassword:
                mAuth.sendPasswordResetEmail(authUserEmailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(Home.this,"Link to change password has been sent to your registered email!",Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            Intent intent1 = new Intent(Home.this,LoginActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                            finish();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(Home.this,"An unexpected error has occured ! Try again Later. " + error,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case R.id.settings:
                Intent intent2 = new Intent(Home.this,UserSettings.class);
                startActivity(intent2);
                break;

        }
        return super.onOptionsItemSelected(item);
    }



}