package com.uptech.magicmodule.utils;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIListener;
import com.uptech.magicmodule.App;

import org.json.JSONObject;

public class AIUIUtil {

    private static final String TAG = "AIUIUtil";

    //交互状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private String room = "";

    public AIUIListener aiuiListener = event -> {
        switch (event.eventType) {
            case AIUIConstant.EVENT_WAKEUP:
                //唤醒事件
                Log.i(TAG, "on event:进入识别状态 " + event.eventType);
                break;

            case AIUIConstant.EVENT_RESULT: {
                //结果事件
                try {
                    JSONObject bizParamJson = new JSONObject(event.info);
                    JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                    JSONObject params = data.getJSONObject("params");
                    JSONObject content = data.getJSONArray("content").getJSONObject(0);

                    if (content.has("cnt_id")) {
                        String cnt_id = content.getString("cnt_id");
                        JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                        String sub = params.optString("sub");
                        JSONObject result = cntJson.optJSONObject("intent");
                        if ("nlp".equals(sub)) {
                            // 解析得到语义结果
                            String str = "";
                            //在线语义结果
                            JSONObject answer = result.optJSONObject("answer");
                            if (answer != null) {
                                str = answer.optString("text");
                                room = "";
                            }
                            else if (result.optInt("rc") == 4) {
                                str = result.optString("text");
                                room = str;
                                str = "好的，接下来我将为您服务";
//                                if (result.optString("intentType").equals("custom")) {
//                                        str = result.optJSONArray("semantic").optJSONObject(0).optJSONArray("slots").optJSONObject(0).optString("value");
//                                        room = str;
//                                        str = "好的，接下来我将会带你去" + str;
//                                    }
                            }
                            else {
                                str = "您的管家有点没听清，主人再说的清楚一点";
                            }
                            if (!TextUtils.isEmpty(str)) {
                                Log.d(TAG, "onEventUtil: " + str);
                                TTSUtils.getInstance().speak(str);
                                getRoomInfo(room);
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            break;

            case AIUIConstant.EVENT_ERROR: {
                //错误事件
                Log.i(TAG, "on event: " + event.eventType);
            }
            break;

            case AIUIConstant.EVENT_VAD: {
                //vad事件
                if (AIUIConstant.VAD_BOS == event.arg1) {
                    Log.d(TAG, "onEvent: 找到语音前端点");
                } else if (AIUIConstant.VAD_EOS == event.arg1) {
                    //
                    Log.d(TAG, "onEvent:找到语音后端点 ");
                } else {
                    Log.d(TAG, "onEvent: " + event.arg2);
                }
            }
            break;

            case AIUIConstant.EVENT_START_RECORD: {
                //开始录音事件
                Log.i(TAG, "on event:开始录音 " + event.eventType);
            }
            break;

            case AIUIConstant.EVENT_STOP_RECORD: {
                //停止录音事件
                Log.i(TAG, "on event: 停止录音" + event.eventType);
            }
            break;

            case AIUIConstant.EVENT_STATE: {    // 状态事件
                mAIUIState = event.arg1;

                if (AIUIConstant.STATE_IDLE == mAIUIState) {
                    // 闲置状态，AIUI未开启
                    Log.d(TAG, "onEvent: 录音等待中");
                } else if (AIUIConstant.STATE_READY == mAIUIState) {
                    // AIUI已就绪，等待唤醒
                    Log.d(TAG, "onEvent: 录音准备中");
                } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                    // AIUI工作中，可进行交互
                    Log.d(TAG, "onEvent: 录音工作中");
                }
            }
            break;
            default:
                break;
        }
    };

    public void getRoomInfo(String str) {

        switch (str){
            case "热门政治新闻":
                str = "国务院总理李克强将于7月1日至2日出席在大连举行的第十三届夏季达沃斯论坛。其间，李克强总理将出席论坛开幕式并发表特别致辞，会见论坛主席施瓦布和与会的外方领导人，并同工商、金融、智库、媒体界人士对话交流。\n" +
                        "本届论坛主题为“领导力4.0：全球化新时代的成功之道”。来自100多个国家的1900余名政、商、学、媒体界代表将参会。\n";
                TTSUtils.getInstance().speak(str);
                break;
            case "热门娱乐新闻":
                str = "由曾国祥执导，周冬雨、易烊千玺主演的新片《少年的你》宣布改档：“经考虑影片《少年的你》的制作完成度和市场预判，经制片、发行各方协商，《少年的你》不在6月27日上映，新的档期择时公布。”";
                TTSUtils.getInstance().speak(str);
                break;
            case "热门体育新闻":
                str = "2018-19赛季颁奖典礼最重磅一个奖项揭晓，阿德托昆博力压詹姆斯-哈登和保罗-乔治，成为常规赛MVP。";
                TTSUtils.getInstance().speak(str);
                break;
                default:
                    break;
        }

    }
}
