import http from './http'

export const listCourses = () => http.get('/courses')
export const getCourse = (id) => http.get(`/courses/${id}`)
