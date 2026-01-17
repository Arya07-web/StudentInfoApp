package com.example.studentinfoapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private ArrayList<Student> studentList;
    private int selectedPosition = -1; // <-- Must be here at class level
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(Student student, int position);
    }

    public void setOnStudentClickListener(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public StudentAdapter(ArrayList<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvName.setText("Name: " + student.name);
        holder.tvRoll.setText("Roll: " + student.rollNo);

        // Highlight selected student
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition(); // <-- safe position
            if (adapterPosition == RecyclerView.NO_POSITION) return; // item removed, ignore

            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onStudentClick(studentList.get(adapterPosition), adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public void clearSelection() {
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        if (previousPosition != -1) notifyItemChanged(previousPosition);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRoll;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRoll = itemView.findViewById(R.id.tvRoll);
        }
    }
}

