package com.totsp.bookworm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.totsp.bookworm.model.Book;

import java.util.ArrayList;
import java.util.Comparator;

public class Main extends Activity {

   private static final int MENU_ABOUT = 0;
   private static final int MENU_BOOKADD = 1;
   private static final int MENU_FILTER = 2;
   private static final int MENU_SORT_RATING = 3;
   private static final int MENU_SORT_ALPHA = 4;
   
   private static final int MENU_CONTEXT_EDIT = 0;
   private static final int MENU_CONTEXT_DELETE = 1;

   private BookWormApplication application;

   private boolean filterVisible;
   private EditText bookListFilter;
   BookAdapter adapter;   
   private ListView bookListView;
   
   private ArrayList<Book> bookList = new ArrayList<Book>();   

   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      this.application = (BookWormApplication) this.getApplication();

      this.setContentView(R.layout.main);     

      this.bookListFilter = (EditText) this.findViewById(R.id.booklistfilter);
      this.bookListFilter.addTextChangedListener(new TextWatcher() {
         public void afterTextChanged(final Editable s) {
         }
         public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
         }
         public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            // TODO the filter doesn't work, should on toString book, but doesn't (need to create own?)
            Main.this.adapter.getFilter().filter(s);
         }
      });
      // TODO collapse/remove until needed?
      this.bookListFilter.setVisibility(View.INVISIBLE);
      
      this.bookListView = (ListView) this.findViewById(R.id.booklistview);    
      // TODO retrieve first X books, then rest in background (rather than all)?
      this.bookList.addAll(this.application.getDataHelper().selectAllBooks());      
      this.bindBookList();
   }

   private void bindBookList() {      
      this.adapter = new BookAdapter(this, android.R.layout.simple_list_item_1, this.bookList);
      this.bookListView.setAdapter(this.adapter);
      this.bookListView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(final AdapterView<?> parent, final View v, final int index, final long id) {
            Main.this.application.setSelectedBook(Main.this.bookList.get(index));
            Main.this.startActivity(new Intent(Main.this, BookDetail.class));
         }
      });
      this.registerForContextMenu(this.bookListView);
   }

   @Override
   public void onPause() {
      super.onPause();
   }

   @Override
   public boolean onCreateOptionsMenu(final Menu menu) {
      menu.add(0, Main.MENU_ABOUT, 0, "About").setIcon(android.R.drawable.ic_menu_help);
      menu.add(0, Main.MENU_BOOKADD, 1, "Add Book").setIcon(android.R.drawable.ic_menu_add);
      menu.add(0, Main.MENU_FILTER, 2, "Filter").setIcon(android.R.drawable.ic_menu_search);
      menu.add(0, Main.MENU_SORT_RATING, 3, "Sort|Rating").setIcon(android.R.drawable.ic_menu_sort_by_size);
      menu.add(0, Main.MENU_SORT_ALPHA, 3, "Sort|Alpha").setIcon(android.R.drawable.ic_menu_sort_alphabetically);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
      switch (item.getItemId()) {
      case MENU_ABOUT:
         this.startActivity(new Intent(Main.this, About.class));
         return true;
      case MENU_BOOKADD:
         this.startActivity(new Intent(Main.this, BookAdd.class));
         return true;
      case MENU_FILTER:
         if (this.filterVisible) {
            this.filterVisible = false;            
            this.bookListFilter.setVisibility(View.INVISIBLE);  
            // TODO animate?            
         } else {
            this.filterVisible = true;            
            this.bookListFilter.setVisibility(View.VISIBLE);
            // TODO animate?
         }         
         return true;
      case MENU_SORT_RATING:         
         this.adapter.sort(new RatingComparator());
         return true;
      case MENU_SORT_ALPHA:
         this.adapter.sort(new AlphaComparator());
         return true;
      default:
         return super.onOptionsItemSelected(item);
      }
   }

   public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.add(0, Main.MENU_CONTEXT_EDIT, 0, "Edit Book");
      menu.add(1, Main.MENU_CONTEXT_DELETE, 0, "Delete Book");
      menu.setHeaderTitle("Action");
   }

   public boolean onContextItemSelected(final MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      long listIndex = info.id;
      final Book b = this.bookList.get((int) listIndex);
      switch (item.getItemId()) {
      case MENU_CONTEXT_EDIT:
         Toast.makeText(this, "TODO Edit Book", Toast.LENGTH_SHORT).show();
         return true;
      case MENU_CONTEXT_DELETE:
         new AlertDialog.Builder(Main.this).setTitle("Delete book?").setMessage(b.getTitle()).setPositiveButton(
                  "Yes, I'm Sure", new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface d, final int i) {
                        Main.this.application.getDataHelper().deleteBook(b.getId());
                        Main.this.startActivity(Main.this.getIntent());
                        Main.this.finish();
                     }
                  }).setNegativeButton("No, Cancel", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface d, final int i) {
            }
         }).show();
         return true;
      default:
         return super.onContextItemSelected(item);
      }
   }

   @Override
   protected void onRestoreInstanceState(final Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
   }

   @Override
   protected void onSaveInstanceState(final Bundle saveState) {
      super.onSaveInstanceState(saveState);
   }
   
   //
   // Sort Comparators
   //

   private class AlphaComparator implements Comparator<Book> {
      public int compare(final Book b1, final Book b2) {
         String title1 = b1.getTitle();
         String title2 = b2.getTitle();
         return title1.toLowerCase().compareTo(title2.toLowerCase());
      }
   }

   private class RatingComparator implements Comparator<Book> {
      public int compare(final Book b1, final Book b2) {
         Integer rat1 = b1.getRating();
         Integer rat2 = b2.getRating();
         return rat1.compareTo(rat2);
      }
   }

   //
   // BookAdapter
   //

   private class BookAdapter extends ArrayAdapter<Book> {

      LayoutInflater vi = (LayoutInflater) Main.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      private final ArrayList<Book> books;

      public BookAdapter(final Context context, final int resId, final ArrayList<Book> books) {
         super(context, resId, books);
         this.books = books;
      }

      @Override
      public View getView(final int position, final View convertView, final ViewGroup parent) {
         View v = convertView;
         if (v == null) {
            v = this.vi.inflate(R.layout.items_list_item, null);
         }

         Book book = this.books.get(position);
         if (book != null) {
            ImageView iv = (ImageView) v.findViewById(R.id.itemslistitemimage);
            if (book.getCoverImageId() > 0) {
               Bitmap coverImage = Main.this.application.getDataImageHelper().getBitmap((int) book.getCoverImageId());
               iv.setImageBitmap(coverImage);
            } else {
               iv.setImageResource(R.drawable.book_cover_missing);
            }
            TextView above = (TextView) v.findViewById(R.id.itemslistitemtextabove);
            above.setText(book.getTitle());
            TextView below = (TextView) v.findViewById(R.id.itemslistitemtextbelow);
            below.setText(book.getSubTitle());
         }
         return v;
      }
   }
}