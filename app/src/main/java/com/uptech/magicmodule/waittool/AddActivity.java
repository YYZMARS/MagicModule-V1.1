package com.uptech.magicmodule.waittool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uptech.magicmodule.R;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {
    private Button back;  //返回按钮
    private EditText title;   //标题
    private EditText context;   //内容
    private Button finish;  //完成按钮
    private String get_title;
    private String get_context;
//    private int note_id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        back = (Button) findViewById(R.id.back_add);
        title = (EditText) findViewById(R.id.title_add);
        context = (EditText) findViewById(R.id.context_add);
        finish = (Button) findViewById(R.id.finish);

        finish.setOnClickListener(this);
        back.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if (view == findViewById(R.id.finish)) {
            NoteOperator noteOperator = new NoteOperator(AddActivity.this);
            get_title = title.getText().toString().trim();
            get_context = context.getText().toString().trim();

            if (TextUtils.isEmpty(get_title) || TextUtils.isEmpty(get_context)) {
                Toast.makeText(AddActivity.this, "添加信息不能为空", Toast.LENGTH_SHORT).show();
            } else {
                Note note = new Note();
                note.title = get_title;
                note.context = get_context;
                boolean add = noteOperator.insert(note);
                //如果添加数据成功，跳到待办事项界面，并通过传值，让目标界面进行刷新
                if (add) {
                    //Toast.makeText(AddActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(AddActivity.this, kkActivity.class);
                    intent.putExtra("Insert", 1);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (view == findViewById(R.id.back_add)) {
            Intent intent = new Intent(AddActivity.this, kkActivity.class);
            startActivity(intent);
            finish();
        }

    }
}




