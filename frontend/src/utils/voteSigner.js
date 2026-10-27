// Client-side ECDSA (P-256) signing for votes.
// The private key is generated in this browser and stored in IndexedDB as a
// NON-extractable CryptoKey: JavaScript can use it to sign, but can never read it out.

const DB_NAME = 'votechain-keys'
const STORE = 'keys'

function openDb() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, 1)
    req.onupgradeneeded = () => req.result.createObjectStore(STORE)
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
  })
}

async function idbGet(key) {
  const db = await openDb()
  return new Promise((resolve, reject) => {
    const req = db.transaction(STORE, 'readonly').objectStore(STORE).get(key)
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
  })
}

async function idbPut(key, value) {
  const db = await openDb()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, 'readwrite')
    tx.objectStore(STORE).put(value, key)
    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })
}

const toBase64 = (buffer) => btoa(String.fromCharCode(...new Uint8Array(buffer)))

/** Identifies the logged-in voter so each account gets its own key in this browser. */
export function currentVoterKey() {
  try {
    const voter = JSON.parse(localStorage.getItem('votechain_voter') || '{}')
    return `voter:${voter.id || voter.email || 'unknown'}`
  } catch {
    return 'voter:unknown'
  }
}

async function getOrCreateKeyPair(keyName) {
  let pair = await idbGet(keyName)
  if (!pair) {
    pair = await crypto.subtle.generateKey(
      { name: 'ECDSA', namedCurve: 'P-256' },
      false, // private key not extractable
      ['sign', 'verify'],
    )
    await idbPut(keyName, pair)
  }
  return pair
}

/** Returns { publicKey, signature } (both base64) for "electionId:candidateId". */
export async function signVote(keyName, electionId, candidateId) {
  if (!window.crypto?.subtle) {
    throw new Error('This browser does not support secure signing (WebCrypto).')
  }
  const pair = await getOrCreateKeyPair(keyName)
  const publicKey = toBase64(await crypto.subtle.exportKey('spki', pair.publicKey))
  const data = new TextEncoder().encode(`${electionId}:${candidateId}`)
  const signature = toBase64(
    await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, pair.privateKey, data),
  )
  return { publicKey, signature }
}