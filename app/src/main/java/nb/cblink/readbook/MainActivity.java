package nb.cblink.readbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ebookdroid.ui.viewer.ViewerActivity;

import java.io.File;
import java.io.FileFilter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        final File[] listFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".djvu")||file.getName().endsWith(".epub")||file.getName().endsWith(".pdf")||file.getName().endsWith(".xps")||file.getName().endsWith(".cbz")||file.getName().endsWith(".fb2");
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listFiles);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File file = listFiles[i];
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
                intent.setClass(MainActivity.this, ViewerActivity.class);
                startActivity(intent);
            }
        });
    }
}
