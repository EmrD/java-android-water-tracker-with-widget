package com.emr.watertracker;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.slider.Slider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HistoryItem {
    private final String historyText;

    public HistoryItem(String historyText) {
        this.historyText = historyText;
    }

    public String getHistoryText() {
        return historyText;
    }
}

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.historyText.setText(historyList.get(position).getHistoryText());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void addItem(String newItem) {
        historyList.add(new HistoryItem(newItem));
        notifyItemInserted(historyList.size() - 1);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView historyText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            historyText = itemView.findViewById(android.R.id.text1);
        }
    }
}

public class MainActivity extends AppCompatActivity {
    private HistoryAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Slider slider = findViewById(R.id.materialSlider);
        TextView waterText = findViewById(R.id.mainWater);
        Button addBtn = findViewById(R.id.materialButton);
        Button refreshBtn = findViewById(R.id.refreshButton);
        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<HistoryItem> historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String userData = sharedPreferences.getString(LocalDate.now().toString(), "0");
                waterText.setText(userData);

                Map<String, ?> allData = sharedPreferences.getAll();

                for (Map.Entry<String, ?> entry : allData.entrySet()) {
                    String date = entry.getKey();
                    String waterAmount = entry.getValue().toString();
                    historyList.add(new HistoryItem(date.replace("-", ".") + "\n " + waterAmount + " ml"));
                }

                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                float waterValue = Float.parseFloat(waterText.getText().toString().replace(" ml", ""));
                try {

                    waterValue = 0;
                    waterText.setText(String.valueOf(waterValue));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        editor.putString(LocalDate.now().toString(), String.valueOf(waterValue));
                    }
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Data refreshed", Toast.LENGTH_LONG).show();
                    MainWidgetProvider.updateWidgetManually(getApplicationContext());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        addBtn.setOnClickListener(v -> {
            if (slider.getValue() != 0) {
                float waterValue = Float.parseFloat(waterText.getText().toString().replace(" ml", ""));

                waterValue += slider.getValue();
                waterText.setText(String.valueOf(waterValue));

                try {
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String today = LocalDate.now().toString();
                        editor.putString(today, waterText.getText().toString());
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_LONG).show();
                        MainWidgetProvider.updateWidgetManually(getApplicationContext());

                        adapter.addItem(today.replace("-", ".") + "\n" + waterText.getText().toString() + " ml");
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}