# Android组件库

## 组件清单

### 1. [二维码扫描-zxing](zxing)

#### 用法说明

```java
    new IntentIntegrator(this)
        .setCustomTitle("二维码扫描") //标题栏文字
        .setPrompt("将扫码框对准二维码开始扫描")  //扫描框底部文字
        .setCameraId(0)  // 后置摄像头
        .setBeepEnabled(true) //是否有提示音
        .setBarcodeImageEnabled(false)
        .setAlbumScanEnabled(true) //是否启用相册扫码
        .initiateScan();
```

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null || result.getContents() == null) {
            return;
        }
        String contents = result.getContents();
        ToastUtil.showToast(contents);

    }
```

### 2. 图表库

#### 用法说明


