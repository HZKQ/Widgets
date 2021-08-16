package com.hzkq.widgets;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.keqiang.views.ChooseItemView;
import com.keqiang.views.DropItemView;
import com.keqiang.views.EditItemView;
import com.keqiang.views.ExtendEditText;
import com.keqiang.views.SpannableString;
import com.keqiang.views.SpannableString.Builder;
import com.keqiang.views.edittext.SimpleTextWatcher;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

/**
 * @author Created by 汪高皖 on 2020/1/14 15:47
 */
public class ViewsActivity extends BaseActivity {
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_views;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
    
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
        ExtendEditText editText = findViewById(R.id.et_test);
        ExtendEditText etTest2 = findViewById(R.id.et_test2);
        etTest2.setNumberOverLimitListener((editText1, isDecimalOver, isIntegerOver) -> {
            if (isDecimalOver && isIntegerOver) {
                ToastUtil.showToast("整数位和小数位长度均超过限制");
            } else if (isDecimalOver) {
                ToastUtil.showToast("小数位超过" + etTest2.getDecimalLimit() + "位长度限制");
            } else if (isIntegerOver) {
                ToastUtil.showToast("整数位超过" + etTest2.getIntegerLimit() + "位长度限制");
            }
        });
        
        etTest2.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                Log.e("xxx", "2:" + s.toString());
                Log.e("xxx", "3:" + etTest2.getText().toString());
            }
        });
        
        EditText etTest10 = findViewById(R.id.et_test10);
        findViewById(R.id.rl_test).setOnClickListener(v -> {
            etTest10.setEnabled(!etTest10.isEnabled());
            // editText.setEnabled(!editText.isEnabled());
            // etTest2.setText("0001234567.987654");
        });
        
        ChooseItemView civItem = findViewById(R.id.civ_item);
        EditItemView eivItem = findViewById(R.id.eiv_item);
        civItem.setOnClickListener(v -> {
            civItem.getTvContent().setText("当前为阅读模式，不可编辑");
            civItem.setShowStyle(ChooseItemView.SHOW_STYLE_READ);
            eivItem.setShowStyle(EditItemView.SHOW_STYLE_READ);
        });
        
        
        eivItem.setOnClickListener(v -> {
            if (eivItem.getShowStyle() == EditItemView.SHOW_STYLE_READ) {
                civItem.getTvContent().setText("点击切换不同的显示模式");
                civItem.setShowStyle(ChooseItemView.SHOW_STYLE_EDIT);
                eivItem.setShowStyle(EditItemView.SHOW_STYLE_EDIT);
            }
        });
        
        DropItemView divDevice = findViewById(R.id.div_device);
        divDevice.setOnDropStatusChangeListener((div, isExpand) -> {
            ToastUtil.showToast(isExpand ? "展开" : "收起");
        });
        
        TextView testView = findViewById(R.id.tv_test4);
        testView.setOnClickListener(v -> {
            SpannableString ss = Builder.appendMode()
                .addSpan("超过一行自动从左到右布局，")
                .addSpan("且根据中文环境自动裁剪中文、English、数字(123456)、emoji表情(\uD83D\uDE02 \uD83D\uDE0B \uD83D\uDE0C)和图片[img]混合的文本 ")
                .color(Color.BLACK)
                .size(SimpleUtil.getScaledValue(80))
                .addSpan("show time better late than never")
                .color(Color.BLUE)
                .couldClick(true)
                .underLine(true)
                .clickListener((view, spanStr) -> {
                    ToastUtil.showToast("点击：" + spanStr);
                })
                .addSpan(" 测试数据 last week")
                .color(Color.GREEN)
                // .addSpan("AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就")
                // .addSpan("AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就")
                // .addSpan("AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就")
                // .addSpan("AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就 AAAAAAAAAAAAAAAAAAA 差看手机端垃圾袋啦；打打蜡卡就得啦地理试卷ad垃圾袋大件垃圾点辣的骄傲；老大；开始；安静的；按时来得快；ad； 啊；ad；sad；啊；打开；萨迪克；阿昆达；了；sad；利率；撒娇的垃圾来得及案例记得老家阿拉丁进垃圾堆里煎熬来得及爱丽丝时间大量卡就")
                // .apply(testView)
                .create();
            ss.setSpan(new ImageSpan(this, R.drawable.ic_launcher_background), 66, 71, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            testView.setMovementMethod(LinkMovementMethod.getInstance());
            testView.setOnLongClickListener(v1 -> true);
            testView.setText(ss);
        });
    }
}
