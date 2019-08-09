package com.xzjmyk.pm.activity.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.core.base.BaseActivity;
import com.core.utils.RecognizerDialogUtil;

/**
 * Created by FANGlh on 2017/1/11.
 * function:
 */
public class SpeechrecognitionActivity extends BaseActivity implements RecognizerDialogListener {

    private static final int DONETOWORD = 12001;
    private EditText identify_words_et;
    private Button speak_start_bt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_recognition);
        setTitle("语音识别");

        identify_words_et = (EditText) findViewById(R.id.sr_identify_words_et);
        speak_start_bt = (Button) findViewById(R.id.sr_speak_start_bt);
        speak_start_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialogUtil.showRecognizerDialog(ct,SpeechrecognitionActivity.this);
            }
        });
    }

    String voicewords = new String();

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        // TODO Auto-generated method stub
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        System.out.println(text);
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
        voicewords = voicewords + text;
        identify_words_et.setText(voicewords);
    }

    @Override
    public void onError(SpeechError speechError) {

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home){
//            Intent getintent = getIntent();
//            if (!StringUtil.isEmpty(getintent.getStringExtra("voice_request")) && "voice_request".equals(getintent.getStringExtra("voice_request"))){
//                Intent intent = new Intent();
//                intent.putExtra("voice_to_word",voicewords);
//                setResult(RESULT_OK,intent);
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
