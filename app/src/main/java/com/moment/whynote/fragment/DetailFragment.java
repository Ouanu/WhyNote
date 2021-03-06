package com.moment.whynote.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleObserver;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moment.oetlib.view.OEditTextView;
import com.moment.oetlib.view.OToolBarView;
import com.moment.oetlib.view.tools.OPictureTool;
import com.moment.oetlib.view.tools.OToolItem;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("ALL")
public class DetailFragment extends Fragment implements View.OnClickListener, LifecycleObserver {


    private static final int OCR_IS_DONE = 1001;
    private static final int DATA_IS_READY = 1000;
    private EditText etTitle;
    private OEditTextView etDesc;
    private OToolBarView toolbar;
    private ResRepository repository;
    private InputMethodManager im;
    private ResData data;
    private static boolean isEditing = false;
    private static final StringBuilder builder = new StringBuilder();
    private DetailHandler handler;
    private TextView bold;
    private TextView italic;
    private TextView addTitle;
    private ImageView addList;
    private ImageView btnGetUri;
    private ImageView btnOCR;
    private Bitmap bitmap;
    private String DcimPath = "";
    private static int startSelect;
    private static boolean chosingPic = false;
    private String text = "";
    private WaitingFragment waitingFragment = new WaitingFragment();

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.i("DetailFragment_fragment_result", "onFragmentResult: ");
                // ???OCR????????????????????????????????????????????????
                text = result.getString("orc_text");
                handler.sendEmptyMessage(OCR_IS_DONE);
            }
        });
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_fragment, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        Bundle bundle = getArguments();
        repository = ResRepository.getInstance();
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        toolbar = view.findViewById(R.id.toolbar);
        etDesc.getEditText().setBackgroundColor(0);
        handler = new DetailHandler(Looper.getMainLooper());
        executor.execute(new LoadDataTask(bundle));
        etDesc.getEditText().getOTools().autoTool();
        etDesc.getEditText().getOTools().addToolItem(new OPictureTool(etDesc.getEditText(), getContext()));

        FloatingActionButton btnModel = view.findViewById(R.id.btn_model);
        btnModel.setOnClickListener(this);
        im = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(etDesc.getEditText().getApplicationWindowToken(), 0);
        etDesc.getEditText().setFocusableInTouchMode(false);

        etDesc.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                builder.replace(0, builder.length(), s.toString());
            }
        });
        bold = view.findViewById(R.id.bold);
        italic = view.findViewById(R.id.italic);
        addTitle = view.findViewById(R.id.add_title);
        addList = view.findViewById(R.id.add_list);
        btnGetUri = view.findViewById(R.id.btn_get_uri);
        btnOCR = view.findViewById(R.id.btn_ocr);

        bold.setOnClickListener(this);
        italic.setOnClickListener(this);
        addTitle.setOnClickListener(this);
        addList.setOnClickListener(this);
        btnGetUri.setOnClickListener(this);
        btnOCR.setOnClickListener(this);

        DcimPath = bundle.getString("dirPath");
    }

    @Override
    public void onStop() {
        super.onStop();
        executor.execute(new SaveDataTask());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_model:
                if (!isEditing) {
                    isEditing = true;
                    toolbar.setVisibility(View.VISIBLE);
                    etDesc.getEditText().setFocusableInTouchMode(true);
                    etDesc.getEditText().setFocusable(true);
                    im.showSoftInput(v, 0);
                    etDesc.getEditText().setText(builder);
                } else {
                    isEditing = false;
                    toolbar.setVisibility(View.GONE);
                    etDesc.getEditText().setFocusable(false);
                    etDesc.getEditText().setFocusableInTouchMode(false);
                    im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    applyTools();
                }
                break;
            case R.id.bold:
                Objects.requireNonNull(etDesc.getEditText().getText()).insert(etDesc.getEditText().getSelectionStart(), "****");
                etDesc.getEditText().setSelection(etDesc.getEditText().getSelectionStart() - 2);
                break;
            case R.id.add_title:
                Objects.requireNonNull(etDesc.getEditText().getText()).insert(etDesc.getEditText().getSelectionStart(), "\n# ");
                etDesc.getEditText().setSelection(etDesc.getEditText().getSelectionStart());
                break;
            case R.id.italic:
                Objects.requireNonNull(etDesc.getEditText().getText()).insert(etDesc.getEditText().getSelectionStart(), "**");
                etDesc.getEditText().setSelection(etDesc.getEditText().getSelectionStart() - 1);
                break;
            case R.id.add_list:
                Objects.requireNonNull(etDesc.getEditText().getText()).insert(etDesc.getEditText().getSelectionStart(), "* ");
                etDesc.getEditText().setSelection(etDesc.getEditText().getSelectionStart());
                break;
            case R.id.btn_get_uri:
                startSelect = etDesc.getEditText().getSelectionStart();
                mGetContent.launch("image/*");
                break;
            case R.id.btn_ocr:
                startSelect = etDesc.getEditText().getSelectionStart();
                PictureFragment pictureFragment = new PictureFragment();
                getParentFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .setCustomAnimations(R.anim.no_slide, R.anim.from_bottom);
                pictureFragment.show(getParentFragmentManager(), "NULL");
            default:
                break;
        }

    }

    /*
    ????????????
     */
    private void applyTools() {
        for (OToolItem oToolItem : etDesc.getEditText().getOTools().getToolList()) {
            oToolItem.applyOMDTool();
        }
    }


    private class DetailHandler extends Handler {

        public DetailHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == DATA_IS_READY) {
                applyTools();
            } else if(msg.what == OCR_IS_DONE) {
                if (startSelect == -1) {
                    etDesc.getEditText().getText().insert(etDesc.getEditText().getText().length(), text);
                } else {
                    etDesc.getEditText().getText().insert(startSelect, text);
                }
            }
        }
    }


    // Activity????????????
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                try {
                    if (result==null) return;
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), result);
                    File saveFile = new File(DcimPath, System.currentTimeMillis() + ".jpg");
                    FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                    // compress - ???????????????
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                    //??????????????????????????????????????????
                    saveImgOut.flush();
                    saveImgOut.close();
                    Log.d("Save Bitmap", "The picture is save to your phone!");
                    if (startSelect == -1) {
                        etDesc.getEditText().getText().insert(etDesc.getEditText().getText().length(), "![Image](" + Uri.fromFile(saveFile) + "\"Image\")\n");
                    } else {
                        etDesc.getEditText().getText().insert(startSelect, "![Image](" + Uri.fromFile(saveFile) + "\"Image\")\n");
                    }
                    chosingPic = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    );


    /**
     * ?????????
     */
    private static Executor executor = new ThreadPoolExecutor(
            2,
            4,
            10,
            TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadPoolExecutor.AbortPolicy());


    /**
     * ????????????
     */
    private class LoadDataTask implements Runnable {

        private Bundle bundle;

        public LoadDataTask(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void run() {
            assert bundle != null;
            if (bundle.getInt("primaryKey") == 0) {
                data = repository.getResDataByUpdateDate(bundle.getLong("updateDate"));
            } else {
                data = repository.getResDataByUid(bundle.getInt("primaryKey"));
            }
            etTitle.setText(data.title);
            etDesc.getEditText().setText(data.desc);
            handler.sendEmptyMessage(DATA_IS_READY);
        }
    }

    /**
     * ????????????
     */
    private class SaveDataTask implements Runnable {

        @Override
        public void run() {
            data.title = String.valueOf(etTitle.getText());
            data.desc = String.valueOf(etDesc.getEditText().getText());
            data.updateDate = System.currentTimeMillis();
            if (data.title.equals("") && data.desc.equals("") && !chosingPic) {
                repository.deleteResData(data);
            } else {
                repository.upResData(data);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
