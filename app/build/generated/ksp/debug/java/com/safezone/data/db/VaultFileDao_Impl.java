package com.safezone.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class VaultFileDao_Impl implements VaultFileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VaultFile> __insertionAdapterOfVaultFile;

  private final EntityDeletionOrUpdateAdapter<VaultFile> __deletionAdapterOfVaultFile;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public VaultFileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVaultFile = new EntityInsertionAdapter<VaultFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vault_files` (`id`,`fileName`,`filePath`,`fileType`,`tag`,`sizeMb`,`durationSeconds`,`createdAt`,`isEncrypted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VaultFile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getFileName());
        statement.bindString(3, entity.getFilePath());
        statement.bindString(4, __FileType_enumToString(entity.getFileType()));
        statement.bindString(5, __FileTag_enumToString(entity.getTag()));
        statement.bindDouble(6, entity.getSizeMb());
        statement.bindLong(7, entity.getDurationSeconds());
        statement.bindLong(8, entity.getCreatedAt());
        final int _tmp = entity.isEncrypted() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__deletionAdapterOfVaultFile = new EntityDeletionOrUpdateAdapter<VaultFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `vault_files` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VaultFile entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM vault_files WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final VaultFile file, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfVaultFile.insertAndReturnId(file);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final VaultFile file, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfVaultFile.handle(file);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VaultFile>> observeAll() {
    final String _sql = "SELECT * FROM vault_files ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vault_files"}, new Callable<List<VaultFile>>() {
      @Override
      @NonNull
      public List<VaultFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileType = CursorUtil.getColumnIndexOrThrow(_cursor, "fileType");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfSizeMb = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeMb");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final List<VaultFile> _result = new ArrayList<VaultFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VaultFile _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final FileType _tmpFileType;
            _tmpFileType = __FileType_stringToEnum(_cursor.getString(_cursorIndexOfFileType));
            final FileTag _tmpTag;
            _tmpTag = __FileTag_stringToEnum(_cursor.getString(_cursorIndexOfTag));
            final double _tmpSizeMb;
            _tmpSizeMb = _cursor.getDouble(_cursorIndexOfSizeMb);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            _item = new VaultFile(_tmpId,_tmpFileName,_tmpFilePath,_tmpFileType,_tmpTag,_tmpSizeMb,_tmpDurationSeconds,_tmpCreatedAt,_tmpIsEncrypted);
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
  public Flow<List<VaultFile>> observeByType(final FileType type) {
    final String _sql = "SELECT * FROM vault_files WHERE fileType = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __FileType_enumToString(type));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vault_files"}, new Callable<List<VaultFile>>() {
      @Override
      @NonNull
      public List<VaultFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileType = CursorUtil.getColumnIndexOrThrow(_cursor, "fileType");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfSizeMb = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeMb");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final List<VaultFile> _result = new ArrayList<VaultFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VaultFile _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final FileType _tmpFileType;
            _tmpFileType = __FileType_stringToEnum(_cursor.getString(_cursorIndexOfFileType));
            final FileTag _tmpTag;
            _tmpTag = __FileTag_stringToEnum(_cursor.getString(_cursorIndexOfTag));
            final double _tmpSizeMb;
            _tmpSizeMb = _cursor.getDouble(_cursorIndexOfSizeMb);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsEncrypted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp != 0;
            _item = new VaultFile(_tmpId,_tmpFileName,_tmpFilePath,_tmpFileType,_tmpTag,_tmpSizeMb,_tmpDurationSeconds,_tmpCreatedAt,_tmpIsEncrypted);
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
  public Object totalStorageMb(final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(sizeMb) FROM vault_files";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM vault_files";
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

  private String __FileType_enumToString(@NonNull final FileType _value) {
    switch (_value) {
      case VIDEO: return "VIDEO";
      case AUDIO: return "AUDIO";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __FileTag_enumToString(@NonNull final FileTag _value) {
    switch (_value) {
      case ENCRYPTED: return "ENCRYPTED";
      case VOICE_LOG: return "VOICE_LOG";
      case HD4K: return "HD4K";
      case SD720: return "SD720";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private FileType __FileType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "VIDEO": return FileType.VIDEO;
      case "AUDIO": return FileType.AUDIO;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private FileTag __FileTag_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ENCRYPTED": return FileTag.ENCRYPTED;
      case "VOICE_LOG": return FileTag.VOICE_LOG;
      case "HD4K": return FileTag.HD4K;
      case "SD720": return FileTag.SD720;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
