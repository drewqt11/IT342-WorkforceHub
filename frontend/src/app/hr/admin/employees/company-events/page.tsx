"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar as CalendarIcon, Plus, X, CheckCircle2, Pencil, Trash2, MapPin, Clock, ChevronLeft, ChevronRight } from "lucide-react"
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isToday, isSameDay, startOfWeek, endOfWeek, addDays, addWeeks, subWeeks, subMonths, addMonths } from "date-fns"
import { authService } from "@/lib/auth"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"
import { Toaster } from "sonner"

interface Event {
  eventId: string
  eventType: string
  title: string
  description: string
  location: string
  eventDatetime: string
  durationHours: number
  isActive: boolean
  createdById: string
  createdByName: string
  enrollmentCount: number
}

export default function CompanyEventsPage() {
  const [events, setEvents] = useState<Event[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [currentDate, setCurrentDate] = useState(new Date())
  const [formData, setFormData] = useState({
    eventType: "",
    title: "",
    description: "",
    location: "",
    eventDatetime: "",
    durationHours: 1,
    isActive: true,
    createdById: ""
  })
  const [viewMode, setViewMode] = useState<'month' | 'week'>('month')

  useEffect(() => {
    fetchEvents()
    const token = authService.getToken()
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]))
      setFormData(prev => ({ ...prev, createdById: payload.sub }))
    }
  }, [])

  const fetchEvents = async () => {
    try {
      setLoading(true)
      const token = authService.getToken()

      if (!token) {
        toast.error("Authentication Error", {
          description: "Please log in to view events"
        })
        throw new Error("No authentication token found")
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/events`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        toast.error("Failed to Load Events", {
          description: errorData?.message || "Please try again later"
        })
        throw new Error(errorData?.message || "Failed to fetch events")
      }

      const data = await response.json()
      setEvents(data)
      toast.success("Events Loaded", {
        description: `Successfully loaded ${data.length} events`
      })
    } catch (err) {
      console.error("Error fetching data:", err)
      setError(err instanceof Error ? err.message : "Failed to fetch data")
    } finally {
      setLoading(false)
    }
  }

  const handleCreateClick = () => {
    setSelectedEvent(null)
    const token = authService.getToken()
    const payload = token ? JSON.parse(atob(token.split('.')[1])) : null
    setFormData({
      eventType: "",
      title: "",
      description: "",
      location: "",
      eventDatetime: "",
      durationHours: 1,
      isActive: true,
      createdById: payload?.sub || ""
    })
    setIsDialogOpen(true)
  }

  const handleEditClick = (event: Event) => {
    setSelectedEvent(event)
    setFormData({
      eventType: event.eventType,
      title: event.title,
      description: event.description || "",
      location: event.location,
      eventDatetime: format(new Date(event.eventDatetime), "yyyy-MM-dd'T'HH:mm"),
      durationHours: event.durationHours,
      isActive: event.isActive,
      createdById: event.createdById
    })
    setIsDialogOpen(true)
  }

  const handleDeleteClick = (event: Event) => {
    setSelectedEvent(event)
    setIsDeleteDialogOpen(true)
  }

  const handleSubmit = async () => {
    try {
      setIsSubmitting(true)
      const token = authService.getToken()

      if (!token) {
        toast.error("Authentication Error", {
          description: "Please log in to manage events"
        })
        throw new Error("No authentication token found")
      }

      // Get user ID from token
      const payload = JSON.parse(atob(token.split('.')[1]))
      const userId = payload.sub

      if (!userId) {
        toast.error("User Error", {
          description: "Unable to identify your user account"
        })
        throw new Error("User ID not found in token")
      }

      const url = selectedEvent 
        ? `${process.env.NEXT_PUBLIC_API_URL}/events/${selectedEvent.eventId}`
        : `${process.env.NEXT_PUBLIC_API_URL}/events`
      
      const method = selectedEvent ? "PUT" : "POST"

      const requestData = {
        ...formData,
        createdById: userId
      }

      const response = await fetch(url, {
        method,
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestData),
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        toast.error("Failed to Save Event", {
          description: errorData?.message || "Please check your input and try again"
        })
        throw new Error(errorData?.message || "Failed to save event")
      }

      const responseData = await response.json()
      toast.success(selectedEvent ? "Event Updated" : "Event Created", {
        description: selectedEvent 
          ? `Successfully updated "${responseData.title}"`
          : `Successfully created "${responseData.title}"`
      })
      setIsDialogOpen(false)
      fetchEvents()
    } catch (err) {
      console.error("Error saving event:", err)
      toast.error("Operation Failed", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!selectedEvent) return

    try {
      setIsSubmitting(true)
      const token = authService.getToken()

      if (!token) {
        toast.error("Authentication Error", {
          description: "Please log in to delete events"
        })
        throw new Error("No authentication token found")
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/events/${selectedEvent.eventId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        toast.error("Failed to Delete Event", {
          description: errorData?.message || "Please try again later"
        })
        throw new Error(errorData?.message || "Failed to delete event")
      }

      toast.success("Event Deleted", {
        description: `Successfully deleted "${selectedEvent.title}"`
      })
      setIsDeleteDialogOpen(false)
      fetchEvents()
    } catch (err) {
      console.error("Error deleting event:", err)
      toast.error("Delete Failed", {
        description: err instanceof Error ? err.message : "An unexpected error occurred"
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const getEventsForDay = (date: Date) => {
    return events.filter(event => isSameDay(new Date(event.eventDatetime), date))
  }

  const renderCalendar = () => {
    const monthStart = startOfMonth(currentDate)
    const monthEnd = endOfMonth(currentDate)
    const days = eachDayOfInterval({ start: monthStart, end: monthEnd })

    return (
      <div className="grid grid-cols-7 gap-1">
        {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map(day => (
          <div key={day} className="text-center font-semibold py-2 text-sm text-gray-600 dark:text-gray-400">
            {day}
          </div>
        ))}
        {days.map(day => {
          const dayEvents = getEventsForDay(day)
          return (
            <div
              key={day.toString()}
              className={cn(
                "min-h-[120px] p-2 border border-gray-200 dark:border-gray-700 rounded-lg",
                !isSameMonth(day, currentDate) && "bg-gray-50 dark:bg-gray-800/50",
                isToday(day) && "bg-blue-50 dark:bg-blue-900/20"
              )}
            >
              <div className="text-sm font-medium mb-1">
                {format(day, "d")}
              </div>
              <div className="space-y-1">
                {dayEvents.map(event => (
                  <div
                    key={event.eventId}
                    className="text-xs p-1 rounded bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-200 cursor-pointer hover:bg-blue-200 dark:hover:bg-blue-900/50"
                    onClick={() => handleEditClick(event)}
                  >
                    <div className="font-medium truncate">{event.title}</div>
                    <div className="text-[10px] text-blue-600 dark:text-blue-300">
                      {format(new Date(event.eventDatetime), "h:mm a")}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )
        })}
      </div>
    )
  }

  const renderWeekView = () => {
    const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 })
    const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 })
    const days = eachDayOfInterval({ start: weekStart, end: weekEnd })

    return (
      <div className="grid grid-cols-7 gap-1 h-[calc(100vh-300px)]">
        {days.map(day => {
          const dayEvents = getEventsForDay(day)
          return (
            <div
              key={day.toString()}
              className={cn(
                "flex flex-col border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden",
                isToday(day) && "bg-blue-50 dark:bg-blue-900/20"
              )}
            >
              <div className="p-2 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
                <div className="text-sm font-medium flex items-center justify-between">
                  <span>{format(day, "EEE")}</span>
                  <span className={cn(
                    "text-sm",
                    isToday(day) && "text-blue-600 dark:text-blue-400 font-bold"
                  )}>
                    {format(day, "d")}
                  </span>
                </div>
              </div>
              <div className="flex-1 overflow-y-auto p-2 space-y-1">
                {dayEvents.length === 0 ? (
                  <div className="text-center text-sm text-gray-500 dark:text-gray-400 py-4">
                    No events
                  </div>
                ) : (
                  dayEvents.map(event => (
                    <div
                      key={event.eventId}
                      className="text-xs p-2 rounded bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-200 cursor-pointer hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors"
                      onClick={() => handleEditClick(event)}
                    >
                      <div className="font-medium truncate">{event.title}</div>
                      <div className="text-[10px] text-blue-600 dark:text-blue-300 mt-1">
                        {format(new Date(event.eventDatetime), "h:mm a")}
                      </div>
                      {event.location && (
                        <div className="text-[10px] text-blue-600 dark:text-blue-300 mt-1 flex items-center gap-1">
                          <MapPin className="h-3 w-3" />
                          <span className="truncate">{event.location}</span>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
          )
        })}
      </div>
    )
  }

  const handlePreviousPeriod = () => {
    if (viewMode === 'month') {
      setCurrentDate(prev => subMonths(prev, 1))
    } else {
      setCurrentDate(prev => subWeeks(prev, 1))
    }
  }

  const handleNextPeriod = () => {
    if (viewMode === 'month') {
      setCurrentDate(prev => addMonths(prev, 1))
    } else {
      setCurrentDate(prev => addWeeks(prev, 1))
    }
  }

  const getPeriodLabel = () => {
    if (viewMode === 'month') {
      return format(currentDate, "MMMM yyyy")
    } else {
      const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 })
      const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 })
      return `${format(weekStart, "MMM d")} - ${format(weekEnd, "MMM d, yyyy")}`
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-6">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div>
              <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
                <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                  <CalendarIcon className="h-5 w-5 text-white" />
                </div>
                Company Events
              </h1>
              <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage company events</p>
            </div>
          </div>
          <Skeleton className="h-[600px] w-full rounded-lg" />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto">
          <div className="text-center py-12 border border-dashed border-[#E5E7EB] dark:border-[#374151] rounded-lg bg-[#F9FAFB] dark:bg-[#111827]/50">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-[#EF4444] to-[#B91C1C] opacity-20 animate-pulse"></div>
              <div className="absolute inset-1 bg-white dark:bg-[#1F2937] rounded-full flex items-center justify-center">
                <CalendarIcon className="h-8 w-8 text-[#EF4444] dark:text-[#EF4444]" />
              </div>
            </div>
            <h3 className="text-xl font-medium text-[#1F2937] dark:text-white mb-2">Error Loading Events</h3>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] max-w-md mx-auto mb-6">{error}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] p-4 md:p-6">
      <Toaster richColors position="top-right" />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[#1F2937] dark:text-white flex items-center gap-2">
              <div className="h-10 w-10 bg-gradient-to-br from-[#3B82F6] to-[#14B8A6] rounded-lg flex items-center justify-center mr-1 shadow-md">
                <CalendarIcon className="h-5 w-5 text-white" />
              </div>
              Company Events
            </h1>
            <p className="text-[#6B7280] dark:text-[#9CA3AF] mt-1">Manage company events</p>
          </div>
          <div className="flex items-center gap-4">
            <Badge
              variant="outline"
              className="bg-[#F0FDFA] text-[#14B8A6] border-[#99F6E4] dark:bg-[#134E4A]/30 dark:text-[#14B8A6] dark:border-[#134E4A] px-3 py-1.5"
            >
              {events.length} events
            </Badge>
            <Button
              onClick={handleCreateClick}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white"
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Event
            </Button>
          </div>
        </div>

        <Card className="border border-[#E5E7EB] dark:border-[#374151] shadow-md">
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg font-semibold text-[#1F2937] dark:text-white">
                {getPeriodLabel()}
              </CardTitle>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setViewMode('month')}
                  className={cn(
                    "text-sm",
                    viewMode === 'month' && "bg-blue-50 dark:bg-blue-900/20"
                  )}
                >
                  Month
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setViewMode('week')}
                  className={cn(
                    "text-sm",
                    viewMode === 'week' && "bg-blue-50 dark:bg-blue-900/20"
                  )}
                >
                  Week
                </Button>
                <div className="flex items-center gap-1">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={handlePreviousPeriod}
                    className="h-8 w-8"
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={handleNextPeriod}
                    className="h-8 w-8"
                  >
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {viewMode === 'month' ? renderCalendar() : renderWeekView()}
          </CardContent>
        </Card>
      </div>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{selectedEvent ? "Edit Event" : "Create Event"}</DialogTitle>
            <DialogDescription>
              {selectedEvent ? "Update the event details below." : "Fill in the details to create a new event."}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="eventType">Event Type</Label>
              <Select
                value={formData.eventType}
                onValueChange={(value) => setFormData({ ...formData, eventType: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select event type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Seminar">Seminar</SelectItem>
                  <SelectItem value="Webinar">Webinar</SelectItem>
                  <SelectItem value="Workshop">Workshop</SelectItem>
                  <SelectItem value="Meeting">Meeting</SelectItem>
                  <SelectItem value="Other">Other</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter event title"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter event description"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                placeholder="Enter event location or URL"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="eventDatetime">Date and Time</Label>
              <Input
                id="eventDatetime"
                type="datetime-local"
                value={formData.eventDatetime}
                onChange={(e) => setFormData({ ...formData, eventDatetime: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="durationHours">Duration (hours)</Label>
              <Input
                id="durationHours"
                type="number"
                min="0.5"
                step="0.5"
                value={formData.durationHours || ""}
                onChange={(e) => {
                  const value = e.target.value === "" ? "" : parseFloat(e.target.value);
                  setFormData({ ...formData, durationHours: value === "" ? 0 : value });
                }}
              />
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="isActive"
                checked={formData.isActive}
                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                className="h-4 w-4 rounded border-gray-300"
              />
              <Label htmlFor="isActive">Active</Label>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488]"
            >
              {isSubmitting ? "Saving..." : (selectedEvent ? "Update Event" : "Create Event")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Delete Event</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this event? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsDeleteDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={handleDelete}
              disabled={isSubmitting}
              className="bg-red-600 hover:bg-red-700 text-white"
            >
              {isSubmitting ? "Deleting..." : "Delete Event"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
