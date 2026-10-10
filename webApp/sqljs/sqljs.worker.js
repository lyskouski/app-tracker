import initSqlJs from "sql.js";

const storageName = "zwyka-sql";
const databaseName = "habits";
const storeName = "databases";

let database;
let transactionOpen = false;

function openStorage() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(storageName, 1);
    request.onupgradeneeded = () => {
      request.result.createObjectStore(storeName);
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function loadDatabase() {
  const storage = await openStorage();
  return new Promise((resolve, reject) => {
    const request = storage
      .transaction(storeName, "readonly")
      .objectStore(storeName)
      .get(databaseName);
    request.onsuccess = () => {
      storage.close();
      resolve(request.result ? new Uint8Array(request.result) : undefined);
    };
    request.onerror = () => {
      storage.close();
      reject(request.error);
    };
  });
}

async function saveDatabase() {
  const storage = await openStorage();
  const bytes = database.export();
  const copy = bytes.buffer.slice(
    bytes.byteOffset,
    bytes.byteOffset + bytes.byteLength,
  );

  await new Promise((resolve, reject) => {
    const transaction = storage.transaction(storeName, "readwrite");
    transaction.objectStore(storeName).put(copy, databaseName);
    transaction.oncomplete = resolve;
    transaction.onerror = () => reject(transaction.error);
    transaction.onabort = () => reject(transaction.error);
  });
  storage.close();
}

async function createDatabase() {
  const SQL = await initSqlJs({ locateFile: () => "/sql-wasm.wasm" });
  database = new SQL.Database(await loadDatabase());
}

async function handleMessage({ data }) {
  let results;
  switch (data && data.action) {
    case "exec":
      if (!data.sql) {
        throw new Error("exec: Missing query string");
      }
      results = database.exec(data.sql, data.params)[0] ?? {
        columns: [],
        values: [],
      };
      if (!transactionOpen) {
        await saveDatabase();
      }
      break;
    case "begin_transaction":
      database.exec("BEGIN TRANSACTION;");
      transactionOpen = true;
      results = [];
      break;
    case "end_transaction":
      database.exec("END TRANSACTION;");
      transactionOpen = false;
      await saveDatabase();
      results = [];
      break;
    case "rollback_transaction":
      database.exec("ROLLBACK TRANSACTION;");
      transactionOpen = false;
      results = [];
      break;
    default:
      throw new Error(`Unsupported action: ${data && data.action}`);
  }

  self.postMessage({ id: data.id, results });
}

const databaseReady = createDatabase();
self.onmessage = (event) => {
  databaseReady
    .then(() => handleMessage(event))
    .catch((error) => {
      self.postMessage({
        id: event.data && event.data.id,
        error: error instanceof Error ? error.message : String(error),
      });
    });
};
