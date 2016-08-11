package kr.co.tacademy.mongsil.mongsil;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class PostPreviewFragment extends DialogFragment {
    private static final String LOCATION = "location";
    private static final String CONTENT = "content";
    private static final String IMGRES = "imgres";

    private String location;
    private String content;
    private int imgRes;


    public PostPreviewFragment() {
    }

    public static PostPreviewFragment newInstance(String location, String content, int imgRes) {
        PostPreviewFragment fragment = new PostPreviewFragment();
        Bundle args = new Bundle();
        args.putString(LOCATION, location);
        args.putString(CONTENT, content);
        args.putInt(IMGRES, imgRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location = getArguments().getString(LOCATION);
            content = getArguments().getString(CONTENT);
            imgRes = getArguments().getInt(IMGRES);
        }
    }

    ImageView imgClose, imgBackground;
    TextView postContent, postLocation, postTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post_preview, container, false);

        imgClose = (ImageView) view.findViewById(R.id.img_close_x);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        imgBackground = (ImageView) view.findViewById(R.id.img_preview_background);
        imgBackground.setBackgroundResource(imgRes);

        postContent = (TextView) view.findViewById(R.id.text_preview_post_content);
        postContent.setText(content);

        postLocation = (TextView) view.findViewById(R.id.text_preview_post_location);
        postLocation.setText(location);

        postTime = (TextView) view.findViewById(R.id.text_preview_post_time);
        postTime.setText(TimeData.PreviewPostTime());


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        window.setAttributes(wlp);
    }

    @Override
    public void onStop() {
        super.onStop();
        dismiss();
    }
}
