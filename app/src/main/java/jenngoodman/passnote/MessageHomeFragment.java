package jenngoodman.passnote;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MessageHomeFragment extends Fragment {

    public static final int CHOOSE_FILE_RESULT_CODE = 1;
    MessageAdapter adapter = null;
    private View view;
    private MessageManager chatManager;
    private TextView chatLine;
    private ListView listView;
    private List<String> items = new ArrayList<String>();
    private ImageView pDisplay;

    @SuppressLint("NewApi")
    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.actvity_messaging_home, container, false);

        pDisplay = (ImageView) view.findViewById(R.id.sharedImage);
        chatLine = (TextView) view.findViewById(R.id.editText);
        listView = (ListView) view.findViewById(android.R.id.list);
        adapter = new MessageAdapter(getActivity(), android.R.id.text1, items);
        listView.setAdapter(adapter);
        view.findViewById(R.id.imageButton).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View arg1) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                }
        );

        view.findViewById(R.id.btn_send).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (chatManager != null) {
                            chatManager.write(chatLine.getText().toString().getBytes());
                            pushMessage("Me: " + chatLine.getText().toString());
                            chatLine.setText("");
                            chatLine.clearFocus();
                        }
                    }
                });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE_RESULT_CODE) {
            if (resultCode == MainActivity.RESULT_OK) {
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    String path;
                    Bitmap photo;
                    Uri mUri = data.getData();

                    path = getRealPathFromURI(getActivity(), mUri);
                    photo = BitmapFactory.decodeFile(path);
                    photo = Bitmap.createScaledBitmap(photo, 128, 128, false);

                    pDisplay.setImageBitmap(photo);
                    pDisplay.invalidate();
                }
            } else if (resultCode == MainActivity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Canceled Selection", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failure!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setChatManager(MessageManager obj) {
        chatManager = obj;
    }

    public void pushMessage(String readMessage) {
        adapter.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    public interface MessageTarget {
        Handler getHandler();
    }

    /**
     * ArrayAdapter to manage chat messages.
     */
    public class MessageAdapter extends ArrayAdapter<String> {

        List<String> messages = null;

        public MessageAdapter(Context context, int textViewResourceId, List<String> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            String message = items.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText = (TextView) v.findViewById(android.R.id.text1);
                if (nameText != null) {
                    nameText.setText(message);
                    if (message.startsWith("Peer:")) {
                        nameText.setTextAppearance(getActivity(), R.style.normalText);
                    } else {
                        nameText.setTextAppearance(getActivity(), R.style.boldText);
                    }
                }
            }
            return v;
        }
    }
}




