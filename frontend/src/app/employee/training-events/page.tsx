"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  GraduationCap,
  Calendar,
  Clock,
  MapPin,
  Users,
  AlertCircle,
  CheckCircle2,
  Filter,
  Search,
  CalendarDays,
  Grid3X3,
  ChevronLeft,
  ChevronRight,
  BookOpen,
  Briefcase,
  Video,
  Award,
  Zap,
  Star,
  User,
} from "lucide-react"
import {
  format,
  startOfWeek,
  endOfWeek,
  eachDayOfInterval,
  addWeeks,
  subWeeks,
  startOfMonth,
  endOfMonth,
  addMonths,
  subMonths,
  isSameDay,
  isToday,
} from "date-fns"
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
import { toast } from "sonner"
import { Toaster } from "sonner"
import { useUser } from "@/contexts/UserContext"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ScrollArea } from "@/components/ui/scroll-area"
import Image from "next/image"

interface TrainingProgram {
  trainingId: string
  title: string
  description: string
  provider: string
  trainingMode: string
  startDate: string
  endDate: string
  maxParticipants: number
  currentParticipants: number
  location: string
  isActive: boolean
}

interface TrainingEvent {
  eventId: string
  eventType: string
  title: string
  description: string
  location: string
  eventDatetime: string
  durationHours: number
  isActive: boolean
  createdBy: {
    userId: string
    emailAddress: string
  }
  currentParticipants?: number
  maxParticipants?: number
}

interface EnrollmentStatus {
  [key: string]: boolean
}

// Helper function to get image based on title or type
const getImageUrl = (item: TrainingProgram | TrainingEvent) => {
  const title = item.title.toLowerCase();
  const type = "eventType" in item
    ? item.eventType.toLowerCase()
    : (item.trainingMode ? item.trainingMode.toLowerCase() : "");

  if (title.includes("leadership") || title.includes("management")) return "/leadership-training-seminar.png";
  if (title.includes("tech") || title.includes("software") || title.includes("coding")) return "/technology-training-coding.png";
  if (title.includes("safety") || title.includes("health")) return "/workplace-safety-training.png";
  if (title.includes("communication") || title.includes("team")) return "/team-communication-workshop.png";
  if (type.includes("workshop")) return "/professional-workshop.png";
  if (type.includes("webinar") || type.includes("online") || type.includes("virtual")) return "/online-webinar-virtual-training.png";
  if (type.includes("conference")) return "/professional-workshop.png";

  // Default images
  return "trainingId" in item
    ? "/professional-workshop.png"
    : "/team-communication-workshop.png";
};

// Helper function to get icon based on training mode or event type
const getTypeIcon = (type: string) => {
  const lowerType = type.toLowerCase()

  if (lowerType.includes("workshop")) return BookOpen
  if (lowerType.includes("webinar") || lowerType.includes("online") || lowerType.includes("virtual")) return Video
  if (lowerType.includes("conference")) return Users
  if (lowerType.includes("certification")) return Award
  if (lowerType.includes("seminar")) return Briefcase
  if (lowerType.includes("course")) return GraduationCap

  return Calendar
}

export default function TrainingEventsPage() {
  const { user } = useUser()
  const [trainingPrograms, setTrainingPrograms] = useState<TrainingProgram[]>([])
  const [trainingEvents, setTrainingEvents] = useState<TrainingEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedItem, setSelectedItem] = useState<TrainingProgram | TrainingEvent | null>(null)
  const [isEnrollDialogOpen, setIsEnrollDialogOpen] = useState(false)
  const [isEnrolling, setIsEnrolling] = useState(false)
  const [enrolledItems, setEnrolledItems] = useState<EnrollmentStatus>({})
  const [viewMode, setViewMode] = useState<"grid" | "calendar">("grid")
  const [calendarView, setCalendarView] = useState<"week" | "month">("week")
  const [currentDate, setCurrentDate] = useState(new Date())
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState("all")
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
  const [activeTab, setActiveTab] = useState("all")

  // Fetch enrollments by employee and set enrolledItems based on status
  const fetchEnrollmentsByEmployee = async (employeeId: string, token: string) => {
    const response = await fetch(`/api/training-enrollments/employee/${employeeId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    if (response.ok) {
      const enrollments = await response.json();
      const enrollmentStatus: EnrollmentStatus = {};
      enrollments.forEach((enrollment: any) => {
        if (enrollment.status === "Enrolled" || enrollment.status === "Completed") {
          if (enrollment.trainingId) {
            enrollmentStatus[enrollment.trainingId] = true;
          }
          if (enrollment.eventId) {
            enrollmentStatus[enrollment.eventId] = true;
          }
        }
      });
      setEnrolledItems(enrollmentStatus);
    }
  };

  useEffect(() => {
    const fetchTrainingData = async () => {
      try {
        setLoading(true)
        const token = authService.getToken()

        if (!token) {
          throw new Error("No authentication token found")
        }

        // Fetch training programs
        const programsResponse = await fetch("/api/training-programs/active-and-not-ended", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })

        if (!programsResponse.ok) {
          throw new Error("Failed to fetch training programs")
        }

        const programsData = await programsResponse.json()
        setTrainingPrograms(programsData)

        // Fetch training events
        const eventsResponse = await fetch("/api/events/active", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })

        if (!eventsResponse.ok) {
          throw new Error("Failed to fetch training events")
        }

        const eventsData = await eventsResponse.json()
        setTrainingEvents(eventsData)

        // Fetch current enrollments using employeeId
        const profile = await authService.getEmployeeProfile()
        await fetchEnrollmentsByEmployee(profile.employeeId, token)
      } catch (err) {
        console.error("Error fetching data:", err)
        setError(err instanceof Error ? err.message : "Failed to fetch data")
      } finally {
        setLoading(false)
      }
    }

    fetchTrainingData()
  }, [])

  const handleEnrollClick = (item: TrainingProgram | TrainingEvent) => {
    setSelectedItem(item)
    setIsEnrollDialogOpen(true)
  }

  const handleDetailsClick = (item: TrainingProgram | TrainingEvent) => {
    setSelectedItem(item)
    setDetailsDialogOpen(true)
  }

  const handleEnroll = async () => {
    if (!selectedItem || !user) return

    try {
      setIsEnrolling(true)
      const token = authService.getToken()

      if (!token) {
        throw new Error("No authentication token found")
      }

      // Get the employee profile to get the employeeId
      const profile = await authService.getEmployeeProfile()

      const isProgram = "trainingId" in selectedItem
      const endpoint = isProgram ? `/api/training-enrollments/self/program` : `/api/training-enrollments/self/event`

      const enrollResponse = await fetch(endpoint, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          employeeId: profile.employeeId,
          ...(isProgram ? { trainingId: selectedItem.trainingId } : { eventId: selectedItem.eventId }),
          enrollmentType: "Self-enrolled",
        }),
      })

      if (!enrollResponse.ok) {
        const errorData = await enrollResponse.json().catch(() => null)
        const errorMessage = errorData?.message || "Failed to enroll"
        if (errorMessage.toLowerCase().includes("already enrolled")) {
          toast.error("You are already enrolled in this training or event.")
        } else {
          toast.error("Failed to enroll", {
            description: errorMessage
          })
        }
        throw new Error(errorMessage)
      }

      // Refresh enrollments after successful enrollment
      await fetchEnrollmentsByEmployee(profile.employeeId, token)

      toast.success("Successfully enrolled")
      setIsEnrollDialogOpen(false)
      setSelectedItem(null)
    } catch (err) {
      console.error("Error enrolling:", err)
      toast.error("Failed to enroll", {
        description: err instanceof Error ? err.message : "An unexpected error occurred",
      })
    } finally {
      setIsEnrolling(false)
    }
  }

  // Calendar navigation functions
  const nextPeriod = () => {
    if (calendarView === "week") {
      setCurrentDate(addWeeks(currentDate, 1))
    } else {
      setCurrentDate(addMonths(currentDate, 1))
    }
  }

  const prevPeriod = () => {
    if (calendarView === "week") {
      setCurrentDate(subWeeks(currentDate, 1))
    } else {
      setCurrentDate(subMonths(currentDate, 1))
    }
  }

  const goToToday = () => {
    setCurrentDate(new Date())
  }

  // Get days for calendar view
  const getDaysForCalendarView = () => {
    if (calendarView === "week") {
      const start = startOfWeek(currentDate, { weekStartsOn: 1 }) // Start on Monday
      const end = endOfWeek(currentDate, { weekStartsOn: 1 })
      return eachDayOfInterval({ start, end })
    } else {
      const start = startOfMonth(currentDate)
      const end = endOfMonth(currentDate)
      return eachDayOfInterval({ start, end })
    }
  }

  // Get events for a specific day
  const getEventsForDay = (day: Date) => {
    const dayStart = new Date(day)
    dayStart.setHours(0, 0, 0, 0)
    const events = trainingEvents.filter((event) => {
      const eventDate = new Date(event.eventDatetime)
      eventDate.setHours(0, 0, 0, 0)
      return eventDate.getTime() === dayStart.getTime()
    })

    const programs = trainingPrograms.filter((program) => {
      const startDate = new Date(program.startDate)
      const endDate = new Date(program.endDate)
      startDate.setHours(0, 0, 0, 0)
      endDate.setHours(0, 0, 0, 0)
      return dayStart >= startDate && dayStart <= endDate
    })

    return [...programs, ...events]
  }

  // Filter items based on search and category
  const filterItems = (items: (TrainingProgram | TrainingEvent)[]) => {
    return items.filter((item) => {
      const matchesSearch =
        item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.description.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesCategory =
        selectedCategory === "all" ||
        (selectedCategory === "programs" && "trainingId" in item) ||
        (selectedCategory === "events" && "eventId" in item)

      const matchesTab =
        activeTab === "all" ||
        (activeTab === "enrolled" && enrolledItems["trainingId" in item ? item.trainingId : item.eventId]) ||
        (activeTab === "upcoming" &&
          ("eventDatetime" in item ? new Date(item.eventDatetime) > new Date() : new Date(item.startDate) > new Date()))

      return matchesSearch && matchesCategory && matchesTab
    })
  }

  const filteredPrograms = filterItems(trainingPrograms)
  const filteredEvents = filterItems(trainingEvents)
  const allFilteredItems = [...filteredPrograms, ...filteredEvents]

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto space-y-6">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <div>
              <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
                <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                  <GraduationCap className="h-6 w-6 text-white" />
                </div>
                Training Hub
              </h1>
              <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
                Discover and enroll in learning opportunities
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="flex flex-col h-full">
                <Skeleton className="h-[300px] w-full rounded-xl" />
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
        <div className="w-full max-w-6xl mx-auto">
          <div className="text-center py-12 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl bg-white/50 dark:bg-slate-800/20 shadow-xl">
            <div className="relative w-20 h-20 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-rose-400 to-red-500 opacity-20 animate-pulse"></div>
              <div className="absolute inset-2 bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                <AlertCircle className="h-10 w-10 text-rose-500 dark:text-rose-400" />
              </div>
            </div>
            <h3 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Error Loading Training Events</h3>
            <p className="text-slate-600 dark:text-slate-300 max-w-md mx-auto mb-6">{error}</p>
            <Button
              className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white shadow-md rounded-full px-6"
              onClick={() => window.location.reload()}
            >
              Try Again
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-sky-50 dark:from-slate-900 dark:via-teal-950 dark:to-slate-950 p-4 md:p-6">
      <Toaster richColors position="top-right" />
      <div className="w-full max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <div className="h-12 w-12 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center mr-2 shadow-lg shadow-teal-200 dark:shadow-teal-900/30">
                <GraduationCap className="h-6 w-6 text-white" />
              </div>
              Training Hub
            </h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1 text-lg">
              Discover and enroll in learning opportunities
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Badge
              variant="outline"
              className="bg-teal-50 text-teal-600 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800 px-3 py-1.5"
            >
              {trainingPrograms.length + trainingEvents.length} available
            </Badge>
            <div className="flex bg-white dark:bg-slate-800 rounded-full p-1 shadow-md border border-slate-200 dark:border-slate-700">
              <Button
                variant="ghost"
                size="sm"
                className={cn(
                  "rounded-full px-3",
                  viewMode === "grid" && "bg-teal-100 text-teal-700 dark:bg-teal-900/50 dark:text-teal-400",
                )}
                onClick={() => setViewMode("grid")}
              >
                <Grid3X3 className="h-4 w-4 mr-1" />
                Grid
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className={cn(
                  "rounded-full px-3",
                  viewMode === "calendar" && "bg-teal-100 text-teal-700 dark:bg-teal-900/50 dark:text-teal-400",
                )}
                onClick={() => setViewMode("calendar")}
              >
                <Calendar className="h-4 w-4 mr-1" />
                Calendar
              </Button>
            </div>
          </div>
        </div>

        {/* Search and Filter */}
        <div className="flex flex-col md:flex-row gap-4 items-center">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-500 dark:text-slate-400" />
            <Input
              placeholder="Search training events..."
              className="pl-10 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus-visible:ring-teal-500 focus-visible:border-teal-500 rounded-full"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-white dark:bg-slate-800 px-4 py-2 rounded-full shadow-md border border-slate-200 dark:border-slate-700">
              <Filter className="h-4 w-4 text-slate-500 dark:text-slate-400" />
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="bg-transparent border-none text-slate-700 dark:text-slate-300 focus:outline-none text-sm"
              >
                <option value="all">All Types</option>
                <option value="programs">Programs</option>
                <option value="events">Events</option>
              </select>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <Tabs defaultValue="all" className="w-full" onValueChange={setActiveTab}>
          <TabsList className="mb-6 bg-white dark:bg-slate-800 p-1.5 rounded-full shadow-md border border-slate-200 dark:border-slate-700 w-full max-w-xl mx-auto flex justify-between">
            <TabsTrigger
              value="all"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <BookOpen className="h-4 w-4 mr-2" />
              All Training
            </TabsTrigger>
            <TabsTrigger
              value="enrolled"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <CheckCircle2 className="h-4 w-4 mr-2" />
              Enrolled
            </TabsTrigger>
            <TabsTrigger
              value="upcoming"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-teal-500 data-[state=active]:to-cyan-500 data-[state=active]:text-white rounded-full transition-all px-6 py-2"
            >
              <Calendar className="h-4 w-4 mr-2" />
              Upcoming
            </TabsTrigger>
          </TabsList>

          {/* Calendar View */}
          {viewMode === "calendar" && (
            <Card className="border-none shadow-xl overflow-hidden bg-white dark:bg-slate-800 rounded-xl mb-6">
              <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-teal-400 to-cyan-500"></div>
              <CardHeader className="flex flex-row items-center justify-between p-4">
                <div className="flex items-center gap-2">
                  <div className="h-10 w-10 bg-gradient-to-br from-teal-400 to-cyan-500 rounded-full flex items-center justify-center shadow-md">
                    <CalendarDays className="h-5 w-5 text-white" />
                  </div>
                  <CardTitle className="text-xl font-bold text-slate-800 dark:text-white">
                    {calendarView === "week"
                      ? `Week of ${format(startOfWeek(currentDate, { weekStartsOn: 1 }), "MMM d, yyyy")}`
                      : format(currentDate, "MMMM yyyy")}
                  </CardTitle>
                </div>
                <div className="flex items-center gap-2">
                  <div className="flex bg-slate-100 dark:bg-slate-700 rounded-full p-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      className={cn("rounded-full px-3", calendarView === "week" && "bg-white dark:bg-slate-600")}
                      onClick={() => setCalendarView("week")}
                    >
                      Week
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className={cn("rounded-full px-3", calendarView === "month" && "bg-white dark:bg-slate-600")}
                      onClick={() => setCalendarView("month")}
                    >
                      Month
                    </Button>
                  </div>
                  <div className="flex items-center gap-1">
                    <Button variant="outline" size="icon" onClick={prevPeriod} className="h-8 w-8 rounded-full">
                      <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" onClick={goToToday} className="h-8 rounded-full px-3">
                      Today
                    </Button>
                    <Button variant="outline" size="icon" onClick={nextPeriod} className="h-8 w-8 rounded-full">
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="p-4">
                <div className={cn("grid gap-2", calendarView === "week" ? "grid-cols-7" : "grid-cols-7")}>
                  {/* Day headers - only show for week view */}
                  {calendarView === "week" &&
                    getDaysForCalendarView().map((day, i) => (
                      <div key={i} className="text-center p-2 font-medium text-slate-600 dark:text-slate-300">
                        {format(day, "EEE")}
                      </div>
                    ))}

                  {/* Calendar days */}
                  {getDaysForCalendarView().map((day, i) => {
                    const dayEvents = getEventsForDay(day)
                    const isCurrentMonth = calendarView === "week" || day.getMonth() === currentDate.getMonth()

                    return (
                      <div
                        key={i}
                        className={cn(
                          "min-h-[100px] border border-slate-200 dark:border-slate-700 rounded-lg p-1",
                          isToday(day) && "bg-teal-50 dark:bg-teal-900/20 border-teal-200 dark:border-teal-800",
                          !isCurrentMonth && "opacity-40",
                        )}
                      >
                        <div className="text-right mb-1">
                          <span
                            className={cn(
                              "inline-block rounded-full w-6 h-6 text-center leading-6 text-xs",
                              isToday(day) && "bg-teal-500 text-white",
                            )}
                          >
                            {format(day, "d")}
                          </span>
                        </div>
                        <ScrollArea className="h-[80px]">
                          <div className="space-y-1">
                            {dayEvents.map((event, j) => (
                              <div
                                key={j}
                                onClick={() => handleDetailsClick(event)}
                                className={cn(
                                  "text-xs p-1 rounded truncate cursor-pointer",
                                  "trainingId" in event
                                    ? "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300"
                                    : "bg-teal-100 text-teal-800 dark:bg-teal-900/30 dark:text-teal-300",
                                )}
                              >
                                {event.title}
                              </div>
                            ))}
                          </div>
                        </ScrollArea>
                      </div>
                    )
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Grid View */}
          {viewMode === "grid" && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {allFilteredItems.length === 0 ? (
                <div className="col-span-full text-center py-12 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl bg-white/50 dark:bg-slate-800/20 shadow-lg">
                  <div className="relative w-20 h-20 mx-auto mb-4">
                    <div className="absolute inset-0 rounded-full bg-gradient-to-r from-teal-400 to-cyan-500 opacity-20 animate-pulse"></div>
                    <div className="absolute inset-2 bg-white dark:bg-slate-800 rounded-full flex items-center justify-center">
                      <GraduationCap className="h-10 w-10 text-slate-500 dark:text-slate-400" />
                    </div>
                  </div>
                  <h3 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">No training found</h3>
                  <p className="text-slate-600 dark:text-slate-300 max-w-md mx-auto mb-6">
                    We couldn't find any training matching your filters.
                  </p>
                  <Button
                    className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white shadow-md rounded-full px-6"
                    onClick={() => {
                      setSearchTerm("")
                      setSelectedCategory("all")
                      setActiveTab("all")
                    }}
                  >
                    Reset Filters
                  </Button>
                </div>
              ) : (
                <>
                  {/* Training Programs and Events */}
                  {allFilteredItems.map((item, idx) => {
                    const isProgram = "trainingId" in item
                    const itemId = isProgram ? item.trainingId : item.eventId
                    const TypeIcon = getTypeIcon(isProgram ? item.trainingMode : item.eventType)
                    const imageUrl = getImageUrl(item)

                    return (
                      <div key={itemId} className="group">
                        <Card className="overflow-hidden border-none shadow-xl hover:shadow-2xl transition-all duration-300 flex flex-col h-full rounded-xl bg-white dark:bg-slate-800">
                          <div className="relative h-48 overflow-hidden">
                            <img
                              src={imageUrl}
                              alt={item.title}
                              className="w-full h-48 object-cover rounded-t-xl"
                            />
                            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                            <Badge
                              className={cn(
                                "absolute top-3 right-3 px-2 py-1",
                                isProgram ? "bg-blue-500 hover:bg-blue-600" : "bg-teal-500 hover:bg-teal-600",
                              )}
                            >
                              {isProgram ? "Program" : "Event"}
                            </Badge>
                            <div className="absolute bottom-3 left-3 right-3">
                              <h3 className="text-white font-bold text-lg line-clamp-2">{item.title}</h3>
                            </div>
                          </div>

                          <CardContent className="p-4 flex-grow">
                            <div className="space-y-3">
                              <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                  <div className="w-8 h-8 rounded-full bg-teal-100 dark:bg-teal-900/30 flex items-center justify-center">
                                    <TypeIcon className="h-4 w-4 text-teal-600 dark:text-teal-400" />
                                  </div>
                                  <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                    {isProgram ? item.trainingMode : item.eventType}
                                  </span>
                                </div>
                                {enrolledItems[itemId] && (
                                  <Badge className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
                                    <CheckCircle2 className="h-3 w-3 mr-1" />
                                    Enrolled
                                  </Badge>
                                )}
                              </div>

                              <div className="space-y-2">
                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-400">
                                  <MapPin className="h-4 w-4 min-w-4 mr-1.5" />
                                  <span className="truncate">{item.location}</span>
                                </div>

                                <div className="flex items-center text-sm text-slate-600 dark:text-slate-400">
                                  <Calendar className="h-4 w-4 min-w-4 mr-1.5" />
                                  <span>
                                    {isProgram
                                      ? `${format(new Date(item.startDate), "MMM d")} - ${format(new Date(item.endDate), "MMM d, yyyy")}`
                                      : format(new Date(item.eventDatetime), "MMM d, yyyy h:mm a")}
                                  </span>
                                </div>

                                {isProgram ? (
                                  <div className="flex items-center text-sm text-slate-600 dark:text-slate-400">
                                    <Users className="h-4 w-4 min-w-4 mr-1.5" />
                                    <span>{item.provider}</span>
                                  </div>
                                ) : (
                                  <div className="flex items-center text-sm text-slate-600 dark:text-slate-400">
                                    <Clock className="h-4 w-4 min-w-4 mr-1.5" />
                                    <span>{item.durationHours} hours</span>
                                  </div>
                                )}
                              </div>

                              <div className="line-clamp-2 text-sm text-slate-600 dark:text-slate-400 min-h-[40px]">
                                {item.description}
                              </div>
                            </div>
                          </CardContent>

                          <CardFooter className="p-4 pt-0 flex gap-2">
                            <Button
                              variant="outline"
                              className="flex-1 border-slate-200 dark:border-slate-700"
                              onClick={() => handleDetailsClick(item)}
                            >
                              Details
                            </Button>
                            <Button
                              className={cn(
                                "flex-1 transition-all duration-200",
                                enrolledItems[itemId]
                                  ? "bg-green-600 hover:bg-green-700 text-white"
                                  : "bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white",
                              )}
                              onClick={() => !enrolledItems[itemId] && handleEnrollClick(item)}
                              disabled={enrolledItems[itemId]}
                            >
                              {enrolledItems[itemId] ? (
                                <span className="flex items-center gap-2">
                                  <CheckCircle2 className="h-4 w-4" />
                                  Enrolled
                                </span>
                              ) : (
                                "Enroll Now"
                              )}
                            </Button>
                          </CardFooter>
                        </Card>
                      </div>
                    )
                  })}
                </>
              )}
            </div>
          )}
        </Tabs>
      </div>

      {/* Enrollment Dialog */}
      <Dialog open={isEnrollDialogOpen} onOpenChange={setIsEnrollDialogOpen}>
        <DialogContent className="sm:max-w-[500px] bg-white dark:bg-slate-800 border-none shadow-xl">
          <DialogHeader>
            <DialogTitle className="text-xl text-slate-800 dark:text-white">
              Enroll in {selectedItem?.title}
            </DialogTitle>
            <DialogDescription className="text-slate-600 dark:text-slate-300">
              Please review the details before confirming your enrollment.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <h4 className="font-medium text-slate-800 dark:text-white">Details</h4>
              <div className="text-sm text-slate-600 dark:text-slate-300 space-y-2">
                {selectedItem && "trainingId" in selectedItem ? (
                  <>
                    <p className="flex items-center gap-2">
                      <Users className="h-4 w-4 text-teal-500" />
                      Provider: {selectedItem.provider}
                    </p>
                    <p className="flex items-center gap-2">
                      <Video className="h-4 w-4 text-teal-500" />
                      Mode: {selectedItem.trainingMode}
                    </p>
                    <p className="flex items-center gap-2">
                      <MapPin className="h-4 w-4 text-teal-500" />
                      Location: {selectedItem.location}
                    </p>
                    <p className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-teal-500" />
                      Start Date: {format(new Date(selectedItem.startDate), "MMM d, yyyy")}
                    </p>
                    <p className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-teal-500" />
                      End Date: {format(new Date(selectedItem.endDate), "MMM d, yyyy")}
                    </p>
                  </>
                ) : (
                  selectedItem && (
                    <>
                      <p className="flex items-center gap-2">
                        <BookOpen className="h-4 w-4 text-teal-500" />
                        Type: {selectedItem.eventType}
                      </p>
                      <p className="flex items-center gap-2">
                        <MapPin className="h-4 w-4 text-teal-500" />
                        Location: {selectedItem.location}
                      </p>
                      <p className="flex items-center gap-2">
                        <Calendar className="h-4 w-4 text-teal-500" />
                        Date: {format(new Date(selectedItem.eventDatetime), "MMM d, yyyy h:mm a")}
                      </p>
                      <p className="flex items-center gap-2">
                        <Clock className="h-4 w-4 text-teal-500" />
                        Duration: {selectedItem.durationHours} hours
                      </p>
                      {selectedItem.createdBy && (
                        <p className="flex items-center gap-2">
                          <User className="h-4 w-4 text-teal-500" />
                          Created by: {selectedItem.createdBy.emailAddress}
                        </p>
                      )}
                    </>
                  )
                )}
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsEnrollDialogOpen(false)}
              className="border-slate-200 dark:border-slate-700"
            >
              Cancel
            </Button>
            <Button
              onClick={handleEnroll}
              disabled={isEnrolling}
              className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600"
            >
              {isEnrolling ? "Enrolling..." : "Confirm Enrollment"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Details Dialog */}
      <Dialog open={detailsDialogOpen} onOpenChange={setDetailsDialogOpen}>
        <DialogContent className="sm:max-w-[600px] bg-white dark:bg-slate-800 border-none shadow-xl">
          {selectedItem && (
            <>
              <DialogHeader>
                <DialogTitle className="sr-only">Training Details</DialogTitle>
                <div className="relative h-48 -mx-6 -mt-6 mb-4 overflow-hidden rounded-t-xl">
                  <Image
                    src={getImageUrl(selectedItem)}
                    alt={selectedItem.title}
                    width={600}
                    height={200}
                    className="w-full h-full object-cover"
                    style={{ display: 'block' }}
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent"></div>
                  <div className="absolute bottom-4 left-6 right-6">
                    <Badge
                      className={cn(
                        "mb-2",
                        "trainingId" in selectedItem
                          ? "bg-blue-500 hover:bg-blue-600"
                          : "bg-teal-500 hover:bg-teal-600",
                      )}
                    >
                      {"trainingId" in selectedItem ? "Program" : "Event"}
                    </Badge>
                    <h2 className="text-white font-bold text-2xl">{selectedItem.title}</h2>
                  </div>
                </div>
                <div className="flex flex-wrap gap-2 mb-2">
                  {"trainingId" in selectedItem ? (
                    <>
                      <Badge
                        variant="outline"
                        className="bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800"
                      >
                        {selectedItem.trainingMode}
                      </Badge>
                      <Badge
                        variant="outline"
                        className="bg-teal-50 text-teal-700 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800"
                      >
                        {selectedItem.provider}
                      </Badge>
                    </>
                  ) : (
                    <>
                      <Badge
                        variant="outline"
                        className="bg-teal-50 text-teal-700 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800"
                      >
                        {selectedItem.eventType}
                      </Badge>
                      <Badge
                        variant="outline"
                        className="bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800"
                      >
                        {selectedItem.durationHours} hours
                      </Badge>
                    </>
                  )}
                  {enrolledItems["trainingId" in selectedItem ? selectedItem.trainingId : selectedItem.eventId] && (
                    <Badge className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400 ml-auto">
                      <CheckCircle2 className="h-3 w-3 mr-1" />
                      Enrolled
                    </Badge>
                  )}
                </div>
              </DialogHeader>

              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">Description</h3>
                  <p className="text-slate-600 dark:text-slate-300 whitespace-pre-wrap">{selectedItem.description}</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">Details</h3>
                    <div className="space-y-2">
                      <div className="flex items-center text-slate-600 dark:text-slate-300">
                        <MapPin className="h-4 w-4 mr-2 text-teal-500" />
                        <span>{selectedItem.location}</span>
                      </div>

                      {"trainingId" in selectedItem ? (
                        <>
                          <div className="flex items-center text-slate-600 dark:text-slate-300">
                            <Calendar className="h-4 w-4 mr-2 text-teal-500" />
                            <span>
                              {format(new Date(selectedItem.startDate), "MMM d, yyyy")} -{" "}
                              {format(new Date(selectedItem.endDate), "MMM d, yyyy")}
                            </span>
                          </div>
                          <div className="flex items-center text-slate-600 dark:text-slate-300">
                            <Users className="h-4 w-4 mr-2 text-teal-500" />
                            <span>
                              {selectedItem.currentParticipants} / {selectedItem.maxParticipants} participants
                            </span>
                          </div>
                        </>
                      ) : (
                        <>
                          <div className="flex items-center text-slate-600 dark:text-slate-300">
                            <Calendar className="h-4 w-4 mr-2 text-teal-500" />
                            <span>{format(new Date(selectedItem.eventDatetime), "MMM d, yyyy")}</span>
                          </div>
                          <div className="flex items-center text-slate-600 dark:text-slate-300">
                            <Clock className="h-4 w-4 mr-2 text-teal-500" />
                            <span>{format(new Date(selectedItem.eventDatetime), "h:mm a")}</span>
                          </div>
                          {selectedItem.currentParticipants !== undefined &&
                            selectedItem.maxParticipants !== undefined && (
                              <div className="flex items-center text-slate-600 dark:text-slate-300">
                                <Users className="h-4 w-4 mr-2 text-teal-500" />
                                <span>
                                  {selectedItem.currentParticipants} / {selectedItem.maxParticipants} participants
                                </span>
                              </div>
                            )}
                        </>
                      )}
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-2">Benefits</h3>
                    <div className="space-y-2">
                      <div className="flex items-center text-slate-600 dark:text-slate-300">
                        <Star className="h-4 w-4 mr-2 text-amber-500" />
                        <span>Professional development</span>
                      </div>
                      <div className="flex items-center text-slate-600 dark:text-slate-300">
                        <Award className="h-4 w-4 mr-2 text-amber-500" />
                        <span>Skill enhancement</span>
                      </div>
                      <div className="flex items-center text-slate-600 dark:text-slate-300">
                        <Zap className="h-4 w-4 mr-2 text-amber-500" />
                        <span>Career advancement</span>
                      </div>
                      <div className="flex items-center text-slate-600 dark:text-slate-300">
                        <Users className="h-4 w-4 mr-2 text-amber-500" />
                        <span>Networking opportunities</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <DialogFooter className="flex gap-2 sm:gap-0">
                <Button
                  variant="outline"
                  onClick={() => setDetailsDialogOpen(false)}
                  className="border-slate-200 dark:border-slate-700"
                >
                  Close
                </Button>
                {!enrolledItems["trainingId" in selectedItem ? selectedItem.trainingId : selectedItem.eventId] && (
                  <Button
                    onClick={() => {
                      setDetailsDialogOpen(false)
                      handleEnrollClick(selectedItem)
                    }}
                    className="bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600"
                  >
                    Enroll Now
                  </Button>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}

