package com.hzkq.widgets;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hzkq.widgets.adapter.UserAdapter;
import com.hzkq.widgets.entity.User;
import com.keqiang.indexbar.IndexBar;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * @author Created by 汪高皖 on 2020/1/8 16:45
 */
public class IndexBarActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private RecyclerView mRv;
    private IndexBar mIndexBar;
    private UserAdapter mUserAdapter;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_index_bar;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mRv = findViewById(R.id.rv);
        mIndexBar = findViewById(R.id.index_bar);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mIndexBar.setTextSize(SimpleUtil.getScaledValue(36));
        mIndexBar.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
    }
    
    @Override
    public void initData() {
        mUserAdapter = new UserAdapter(null);
        mRv.setAdapter(mUserAdapter);
        
        List<User> list = new ArrayList<>();
        User user = new User("张三", "17681110000");
        list.add(user);
        
        user = new User("张军", "17681110009");
        list.add(user);
        
        user = new User("张淑韵", "17681110001");
        list.add(user);
    
        for (int i = 0; i < 10; i++) {
            user = new User("@" + i, "1868111001" + i);
            list.add(user);
        }
        
        user = new User("汪涵", "17681110008");
        list.add(user);
        
        user = new User("汪峰", "17681110012");
        list.add(user);
        
        user = new User("周二珂", "17681110016");
        list.add(user);
        
        user = new User("宋江", "17681110016");
        list.add(user);
        
        user = new User("宋美丽", "17681110016");
        list.add(user);
        
        user = new User("宋爱国", "17681110016");
        list.add(user);
        
        user = new User("王五", "17681110016");
        list.add(user);
        
        user = new User("王二板", "17681110016");
        list.add(user);
        
        for (int i = 0; i < 10; i++) {
            user = new User("李" + i, "1358111001" + i);
            list.add(user);
        }
    
        for (int i = 0; i < 10; i++) {
            user = new User("胡" + i, "1958111001" + i);
            list.add(user);
        }
    
        for (int i = 0; i < 10; i++) {
            user = new User("刘" + i, "1238111001" + i);
            list.add(user);
        }
    
        mUserAdapter.setNewData(list);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        // mUserAdapter.setIndexDoneListener(indexer -> mIndexBar.setLetters(indexer.getSections()));
        
        mIndexBar.setLetterTouchListener(new IndexBar.OnLetterTouchListener() {
            @Override
            public void onLetterTouch(String letter, int position) {
                int pos = mUserAdapter.getPositionForLetter(letter);
                ((LinearLayoutManager) mRv.getLayoutManager()).scrollToPositionWithOffset(pos, 0);
            }
            
            @Override
            public void onActionUp() {
            
            }
        });
    }
}
