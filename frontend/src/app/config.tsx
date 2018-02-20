import { mapValues } from 'lodash'

const rootId = 'root'
const root = document.getElementById(rootId)

export const API_URL = root.getAttribute('data-api-url')

export const auth = {
  loggedIn: false,
  username: undefined,
  key: undefined
}
