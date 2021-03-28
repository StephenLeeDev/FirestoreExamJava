package com.example.firestoreexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;

    private EditText editTextName;
    private EditText editTextEmpId;

    private String name;
    private String empId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);

        editTextName = findViewById(R.id.editTextName);
        editTextEmpId = findViewById(R.id.editTextEmpId);
        findViewById(R.id.button).setOnClickListener(this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(new Date());

        Query query = firestore.collection("Users").orderBy("empId");

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<UserModel, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
                holder.textViewName.setText("Name : " + model.getName());
                holder.textViewEmpId.setText("EmpId : " + model.getEmpId());
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewEmpId;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmpId = itemView.findViewById(R.id.textViewEmpId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: {
                if (userValidation()) {
                    firestore.collection("Users").add(getUser())
                            .addOnSuccessListener(documentReference -> {
                                        editTextName.setText("");
                                        editTextEmpId.setText("");
                                        Toast.makeText(context, getString(R.string.user_added_successfully), Toast.LENGTH_SHORT).show();
                                    }
                            ).addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }
    }

    private boolean userValidation() {
        name = editTextName.getText().toString();
        empId = editTextEmpId.getText().toString();

        if ("".equals(name)) {
            Toast.makeText(context, getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return false;
        } else if ("".equals(empId)) {
            Toast.makeText(context, getString(R.string.enter_empId), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private UserModel getUser() {
        UserModel userModel = new UserModel(name, empId);
        return userModel;
    }
}