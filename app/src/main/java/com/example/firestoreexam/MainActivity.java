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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String NAME = "name";
    private final String EMP_ID = "empId";
    private final String USERS = "Users";
    private final String EVENT = "Event";

    private Context context;

    private FirebaseFirestore fireStore;
    private CollectionReference collectionReference;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;

    private EditText editTextName;
    private EditText editTextEmpId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        fireStore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);

        editTextName = findViewById(R.id.editTextName);
        editTextEmpId = findViewById(R.id.editTextEmpId);
        findViewById(R.id.button).setOnClickListener(this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(new Date());

        collectionReference = fireStore.collection(EVENT).document(today).collection(USERS);
        Query query = collectionReference.orderBy(EMP_ID);

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
                String userName = "Name : " + model.getName();
                String userEmpId = "EmpId : " + model.getEmpId();
                holder.textViewName.setText(userName);
                holder.textViewEmpId.setText(userEmpId);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewEmpId;

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
                String name = editTextName.getText().toString();
                String empId = editTextEmpId.getText().toString();
                if (userValidation(name, empId)) {
                    empIdDuplicationValidation(name, empId);
                }
            }
        }
    }

    private boolean userValidation(String name, String empId) {
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

    private void empIdDuplicationValidation(String name, String empId) {
        collectionReference.whereEqualTo(EMP_ID, empId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapShot = task.getResult();
                if (snapShot.isEmpty()) {
                    collectionReference.add(getUser(name, empId))
                            .addOnSuccessListener(documentReference -> {
                                        editTextName.setText("");
                                        editTextEmpId.setText("");
                                        Toast.makeText(context, getString(R.string.user_added_successfully), Toast.LENGTH_SHORT).show();
                                    }
                            ).addOnFailureListener(e -> {
                        Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(context, R.string.already_applied, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private UserModel getUser(String name, String empId) {
        UserModel userModel = new UserModel(name, empId);
        return userModel;
    }
}