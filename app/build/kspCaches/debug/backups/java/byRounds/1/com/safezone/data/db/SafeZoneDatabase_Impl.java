package com.safezone.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SafeZoneDatabase_Impl extends SafeZoneDatabase {
  private volatile ContactDao _contactDao;

  private volatile TriggerPhraseDao _triggerPhraseDao;

  private volatile VaultFileDao _vaultFileDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `contacts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `slot` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trigger_phrases` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `phrase` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `slot` INTEGER NOT NULL, `voicePrintPath` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vault_files` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileName` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileType` TEXT NOT NULL, `tag` TEXT NOT NULL, `sizeMb` REAL NOT NULL, `durationSeconds` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `isEncrypted` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b66272d67e4cac5f0c120e8f8aa50bbf')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `contacts`");
        db.execSQL("DROP TABLE IF EXISTS `trigger_phrases`");
        db.execSQL("DROP TABLE IF EXISTS `vault_files`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsContacts = new HashMap<String, TableInfo.Column>(5);
        _columnsContacts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("slot", new TableInfo.Column("slot", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("isSynced", new TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesContacts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoContacts = new TableInfo("contacts", _columnsContacts, _foreignKeysContacts, _indicesContacts);
        final TableInfo _existingContacts = TableInfo.read(db, "contacts");
        if (!_infoContacts.equals(_existingContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "contacts(com.safezone.data.db.Contact).\n"
                  + " Expected:\n" + _infoContacts + "\n"
                  + " Found:\n" + _existingContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsTriggerPhrases = new HashMap<String, TableInfo.Column>(5);
        _columnsTriggerPhrases.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTriggerPhrases.put("phrase", new TableInfo.Column("phrase", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTriggerPhrases.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTriggerPhrases.put("slot", new TableInfo.Column("slot", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTriggerPhrases.put("voicePrintPath", new TableInfo.Column("voicePrintPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTriggerPhrases = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTriggerPhrases = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTriggerPhrases = new TableInfo("trigger_phrases", _columnsTriggerPhrases, _foreignKeysTriggerPhrases, _indicesTriggerPhrases);
        final TableInfo _existingTriggerPhrases = TableInfo.read(db, "trigger_phrases");
        if (!_infoTriggerPhrases.equals(_existingTriggerPhrases)) {
          return new RoomOpenHelper.ValidationResult(false, "trigger_phrases(com.safezone.data.db.TriggerPhrase).\n"
                  + " Expected:\n" + _infoTriggerPhrases + "\n"
                  + " Found:\n" + _existingTriggerPhrases);
        }
        final HashMap<String, TableInfo.Column> _columnsVaultFiles = new HashMap<String, TableInfo.Column>(9);
        _columnsVaultFiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("fileType", new TableInfo.Column("fileType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("tag", new TableInfo.Column("tag", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("sizeMb", new TableInfo.Column("sizeMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("durationSeconds", new TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFiles.put("isEncrypted", new TableInfo.Column("isEncrypted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaultFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaultFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaultFiles = new TableInfo("vault_files", _columnsVaultFiles, _foreignKeysVaultFiles, _indicesVaultFiles);
        final TableInfo _existingVaultFiles = TableInfo.read(db, "vault_files");
        if (!_infoVaultFiles.equals(_existingVaultFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "vault_files(com.safezone.data.db.VaultFile).\n"
                  + " Expected:\n" + _infoVaultFiles + "\n"
                  + " Found:\n" + _existingVaultFiles);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b66272d67e4cac5f0c120e8f8aa50bbf", "908d60a8168ee18eab4c38672de6a352");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "contacts","trigger_phrases","vault_files");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `contacts`");
      _db.execSQL("DELETE FROM `trigger_phrases`");
      _db.execSQL("DELETE FROM `vault_files`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ContactDao.class, ContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TriggerPhraseDao.class, TriggerPhraseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VaultFileDao.class, VaultFileDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ContactDao contactDao() {
    if (_contactDao != null) {
      return _contactDao;
    } else {
      synchronized(this) {
        if(_contactDao == null) {
          _contactDao = new ContactDao_Impl(this);
        }
        return _contactDao;
      }
    }
  }

  @Override
  public TriggerPhraseDao triggerPhraseDao() {
    if (_triggerPhraseDao != null) {
      return _triggerPhraseDao;
    } else {
      synchronized(this) {
        if(_triggerPhraseDao == null) {
          _triggerPhraseDao = new TriggerPhraseDao_Impl(this);
        }
        return _triggerPhraseDao;
      }
    }
  }

  @Override
  public VaultFileDao vaultFileDao() {
    if (_vaultFileDao != null) {
      return _vaultFileDao;
    } else {
      synchronized(this) {
        if(_vaultFileDao == null) {
          _vaultFileDao = new VaultFileDao_Impl(this);
        }
        return _vaultFileDao;
      }
    }
  }
}
