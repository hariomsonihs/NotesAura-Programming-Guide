package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminUserAdapter;
import com.hariomsonihs.notesaura.models.User;

public class AdminUsersActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private AdminUserAdapter userAdapter;
    private List<User> users;
    private List<User> filteredUsers;
    private FirebaseFirestore db;
    private android.widget.EditText searchUsersEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }
    
    private void initializeViews() {
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        searchUsersEditText = findViewById(R.id.search_users);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("👥 All Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        users = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        userAdapter = new AdminUserAdapter(filteredUsers, user -> {
            Intent intent = new Intent(this, AdminUserDetailActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("user_name", user.getName());
            intent.putExtra("user_email", user.getEmail());
            startActivity(intent);
        }, user -> {
            // Long press to delete user
            showDeleteUserDialog(user);
        });
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void setupSearch() {
        searchUsersEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(users);
        } else {
            String lower = query.toLowerCase();
            for (User user : users) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lower)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lower)) ||
                    (user.getPhone() != null && user.getPhone().toLowerCase().contains(lower))) {
                    filteredUsers.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }
    
    private void loadUsers() {
        db.collection("users")
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    // Handle error
                    return;
                }
                users.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    User user = new User();
                    user.setId(document.getId());
                    user.setName(document.getString("name"));
                    user.setEmail(document.getString("email"));
                    user.setPhone(document.getString("phone"));
                    user.setJoiningDate(document.getDate("joiningDate"));
                    user.setAdmin("yes".equals(document.getString("admin")));
                    users.add(user);
                }
                filterUsers(searchUsersEditText.getText().toString());
            });
    }
    
    private void showDeleteUserDialog(User user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("⚠️ Delete User");
        builder.setMessage("Are you sure you want to permanently delete user:\n\n" + 
                          user.getName() + "\n" + user.getEmail() + 
                          "\n\nThis action cannot be undone!");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteUser(user);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteUser(User user) {
        // Delete user document and all subcollections
        db.collection("users").document(user.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                loadUsers(); // Refresh the list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}