import { assign } from 'lodash'
export const buildAPIRequest = (endpoint, method, headers, types, body) => {
  const result = {
    endpoint,
    method,
    headers,
    credentials: 'include',
    types,
    body: null
  }

  if (body) {
    result.body = body
  }
  return result
}

export const getJsonHeadersWithoutAuth = () => {
  return buildJsonHeaders()
}

export const getJsonHeaders = () => {
  const result = assign(getHeaders(), buildJsonHeaders())
  return result
}

const buildJsonHeaders = () => {
  return {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
}

export const getHeaders = () => {
  return {
    'x-auth-token': 'token',
    'x-auth-user': 'user'
  }
}
