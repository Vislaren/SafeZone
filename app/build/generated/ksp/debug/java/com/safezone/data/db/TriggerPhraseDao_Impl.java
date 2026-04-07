package com.safezone.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TriggerPhraseDao_Impl implements TriggerPhraseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TriggerPhrase> __insertionAdapterOfTriggerPhrase;

  private final EntityDeletionOrUpdateAdapter<TriggerPhrase> __deletionAdapterOfTriggerPhrase;

  private final EntityDeletionOrUpdateAdapter<TriggerPhrase> __updateAdapterOfTriggerPhrase;

  public TriggerPhraseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTriggerPhrase = new EntityInsertionAdapter<TriggerPhrase>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `trigger_phrases` (`id`,`phrase`,`isActive`,`slot`,`voicePrintPath`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TriggerPhrase entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPhrase());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getSlot());
        if (entity.getVoicePrintPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVoicePrintPath());
        }
      }
    };
    this.__deletionAdapterOfTriggerPhrase = new EntityDeletionOrUpdateAdapter<TriggerPhrase>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `trigger_phrases` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TriggerPhrase entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTriggerPhrase = new EntityDeletionOrUpdateAdapter<TriggerPhrase>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trigger_phrases` SET `id` = ?,`phrase` = ?,`isActive` = ?,`slot` = ?,`voicePrintPath` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TriggerPhrase entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPhrase());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getSlot());
        if (entity.getVoicePrintPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVoicePrintPath());
        }
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final TriggerPhrase phrase, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTriggerPhrase.insertAndReturnId(phrase);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TriggerPhrase phrase, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTriggerPhrase.handle(phrase);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TriggerPhrase phrase, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTriggerPhrase.handle(phrase);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TriggerPhrase>> observeActive() {
    final String _sql = "SELECT * FROM trigger_phrases WHERE isActive = 1 ORDER BY slot ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trigger_phrases"}, new Callable<List<TriggerPhrase>>() {
      @Override
      @NonNull
      public List<TriggerPhrase> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPhrase = CursorUtil.getColumnIndexOrThrow(_cursor, "phrase");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfVoicePrintPath = CursorUtil.getColumnIndexOrThrow(_cursor, "voicePrintPath");
          final List<TriggerPhrase> _result = new ArrayList<TriggerPhrase>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TriggerPhrase _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpPhrase;
            _tmpPhrase = _cursor.getString(_cursorIndexOfPhrase);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final int _tmpSlot;
            _tmpSlot = _cursor.getInt(_cursorIndexOfSlot);
            final String _tmpVoicePrintPath;
            if (_cursor.isNull(_cursorIndexOfVoicePrintPath)) {
              _tmpVoicePrintPath = null;
            } else {
              _tmpVoicePrintPath = _cursor.getString(_cursorIndexOfVoicePrintPath);
            }
            _item = new TriggerPhrase(_tmpId,_tmpPhrase,_tmpIsActive,_tmpSlot,_tmpVoicePrintPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActive(final Continuation<? super List<TriggerPhrase>> $completion) {
    final String _sql = "SELECT * FROM trigger_phrases WHERE isActive = 1 ORDER BY slot ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TriggerPhrase>>() {
      @Override
      @NonNull
      public List<TriggerPhrase> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPhrase = CursorUtil.getColumnIndexOrThrow(_cursor, "phrase");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfVoicePrintPath = CursorUtil.getColumnIndexOrThrow(_cursor, "voicePrintPath");
          final List<TriggerPhrase> _result = new ArrayList<TriggerPhrase>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TriggerPhrase _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpPhrase;
            _tmpPhrase = _cursor.getString(_cursorIndexOfPhrase);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final int _tmpSlot;
            _tmpSlot = _cursor.getInt(_cursorIndexOfSlot);
            final String _tmpVoicePrintPath;
            if (_cursor.isNull(_cursorIndexOfVoicePrintPath)) {
              _tmpVoicePrintPath = null;
            } else {
              _tmpVoicePrintPath = _cursor.getString(_cursorIndexOfVoicePrintPath);
            }
            _item = new TriggerPhrase(_tmpId,_tmpPhrase,_tmpIsActive,_tmpSlot,_tmpVoicePrintPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<TriggerPhrase>> $completion) {
    final String _sql = "SELECT * FROM trigger_phrases ORDER BY slot ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TriggerPhrase>>() {
      @Override
      @NonNull
      public List<TriggerPhrase> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPhrase = CursorUtil.getColumnIndexOrThrow(_cursor, "phrase");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "slot");
          final int _cursorIndexOfVoicePrintPath = CursorUtil.getColumnIndexOrThrow(_cursor, "voicePrintPath");
          final List<TriggerPhrase> _result = new ArrayList<TriggerPhrase>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TriggerPhrase _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpPhrase;
            _tmpPhrase = _cursor.getString(_cursorIndexOfPhrase);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final int _tmpSlot;
            _tmpSlot = _cursor.getInt(_cursorIndexOfSlot);
            final String _tmpVoicePrintPath;
            if (_cursor.isNull(_cursorIndexOfVoicePrintPath)) {
              _tmpVoicePrintPath = null;
            } else {
              _tmpVoicePrintPath = _cursor.getString(_cursorIndexOfVoicePrintPath);
            }
            _item = new TriggerPhrase(_tmpId,_tmpPhrase,_tmpIsActive,_tmpSlot,_tmpVoicePrintPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object activeCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM trigger_phrases WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
