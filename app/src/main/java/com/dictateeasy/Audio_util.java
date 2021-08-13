package com.dictateeasy;

import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by crazy_programmer on 9/25/2017.
 */

public class Audio_util {

    private String dest_file_name;
    private String merged_file_name="";
    public Audio_util(String dest_name,String mergedFileName) {
        dest_file_name = dest_name;
        merged_file_name=mergedFileName;
    }


    public String ConcatAudios(File... mp3Files) {

        if(mp3Files == null) return null;

        File mergedFile = new File(merged_file_name);

        for(File fi : mp3Files){
            Log.e("Mod dates",""+fi.lastModified());
        }

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // sort the array for audio sequence

        if (mp3Files.length > 1) {
            Arrays.sort(mp3Files, new Comparator<File>() {
                @Override
                public int compare(File object1, File object2) {
                    return Long.valueOf(object1.lastModified()).compareTo(object2.lastModified());
                }
            });
        }
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


        for(File fi : mp3Files){
            Log.e("Mod dates 2nd",""+fi.lastModified());
        }

        FileOutputStream fos = null;
        String file_name = null;
        try {
            Log.e("mergefileexist",mergedFile.exists()+"");
            if (!mergedFile.exists()) {
                Log.e("mergefileexist","before"+mergedFile.getName());
                mergedFile.createNewFile();
                Log.e("mergefileexist","after");
            }
            Log.e("testhi","hi1");
            Movie[] movies = new Movie[mp3Files.length];
            for (int i = 0; i < mp3Files.length; i++) {
                movies[i] = MovieCreator.build(mp3Files[i].getAbsolutePath());
            }
            Log.e("testhi","hi2");
            final Movie finalMovie = new Movie();

            List<Track> audioTracks = new ArrayList<>();
            for (Movie movie : movies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals("soun")) {
                        audioTracks.add(track);
                    }
                }
            }
            Log.e("testhi","hi3");
            finalMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

            final Container container = new DefaultMp4Builder().build(finalMovie);
            FileChannel fc = new RandomAccessFile(mergedFile, "rw").getChannel();
            container.writeContainer(fc);
            fc.close();
            Log.e("testhi","hi4");
            Log.e("mergefileexist2nd",(mergedFile.exists() && mergedFile.length() > 1)+"");
            if (mergedFile.exists() && mergedFile.length() > 1) {
                //delete single files
                for(File fi : mp3Files){
                    Log.e("Mod dates 2nd",""+fi.getName());
                    fi.delete();
                }
                mergedFile.renameTo(new File(dest_file_name));
                file_name = dest_file_name;
            }

        } catch (Exception e) {
            Log.e("mp3Files main excpt", e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file_name;
    }


}
