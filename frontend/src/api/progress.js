import http from './http'

export const getSummary = () => http.get('/progress/summary')
export const getCourseProgress = (courseId) => http.get(`/progress/course/${courseId}`)
export const toggleLesson = (lessonId) => http.post(`/progress/${lessonId}/toggle`)
