"use client"

import { useState, useEffect, useRef } from "react"
import {
  Clock,
  CheckCircle,
  XCircle,
  Calendar,
  History,
  Coffee,
  AlertCircle,
  PlayCircle,
  PauseCircle,
  Timer,
  ChevronDown,
  ChevronUp,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Alert, AlertDescription } from "@/components/ui/alert"

type ClockAction = "in" | "out" | "break_start" | "break_end"

type ClockRecord = {
  action: ClockAction
  time: string
  timestamp: number
}

export function ClockInOut() {
  const [status, setStatus] = useState<"in" | "out">("out")
  const [breakStatus, setBreakStatus] = useState<"active" | "inactive">("inactive")
  const [currentTime, setCurrentTime] = useState<string>("")
  const [currentDate, setCurrentDate] = useState<string>("")
  const [seconds, setSeconds] = useState<number>(0)
  const [history, setHistory] = useState<ClockRecord[]>([])
  const [showHistory, setShowHistory] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [breakStartTime, setBreakStartTime] = useState<number | null>(null)
  const [breakElapsedTime, setBreakElapsedTime] = useState<string>("00:00")
  const [totalBreakTime, setTotalBreakTime] = useState<number>(0)
  const [totalBreakDisplay, setTotalBreakDisplay] = useState<string>("00:00")
  const [todayClockIn, setTodayClockIn] = useState<boolean>(false)
  const breakTimerRef = useRef<NodeJS.Timeout | null>(null)
  const [workStartTime, setWorkStartTime] = useState<number | null>(null)
  const [totalWorkTime, setTotalWorkTime] = useState<number>(0)
  const [elapsedWorkTime, setElapsedWorkTime] = useState<string>("00:00:00")
  const workTimerRef = useRef<NodeJS.Timeout | null>(null)

  // Format milliseconds to MM:SS
  const formatElapsedTime = (ms: number): string => {
    const minutes = Math.floor(ms / 60000)
    const seconds = Math.floor((ms % 60000) / 1000)
    return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`
  }

  // Format milliseconds to HH:MM:SS
  const formatWorkTime = (ms: number): string => {
    const hours = Math.floor(ms / 3600000)
    const minutes = Math.floor((ms % 3600000) / 60000)
    const seconds = Math.floor((ms % 60000) / 1000)
    return `${hours.toString().padStart(2, "0")}:${minutes
      .toString()
      .padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`
  }

  useEffect(() => {
    const updateTime = () => {
      const now = new Date()
      setCurrentTime(
        now.toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        }),
      )
      setCurrentDate(
        now.toLocaleDateString([], {
          weekday: "long",
          month: "long",
          day: "numeric",
        }),
      )
      setSeconds(now.getSeconds())
    }

    updateTime()
    const timer = setInterval(updateTime, 1000)

    return () => clearInterval(timer)
  }, [])

  // Break timer effect
  useEffect(() => {
    if (breakStatus === "active" && breakStartTime) {
      breakTimerRef.current = setInterval(() => {
        const currentBreakTime = Date.now() - breakStartTime
        setBreakElapsedTime(formatElapsedTime(currentBreakTime))
        // Update total break time in real-time
        setTotalBreakDisplay(formatElapsedTime(totalBreakTime + currentBreakTime))
      }, 1000)
    } else if (breakTimerRef.current) {
      clearInterval(breakTimerRef.current)
    }

    return () => {
      if (breakTimerRef.current) {
        clearInterval(breakTimerRef.current)
      }
    }
  }, [breakStatus, breakStartTime, totalBreakTime])

  // Work time tracking effect
  useEffect(() => {
    if (status === "in" && breakStatus === "inactive" && workStartTime) {
      workTimerRef.current = setInterval(() => {
        const elapsed = Date.now() - workStartTime + totalWorkTime
        setElapsedWorkTime(formatWorkTime(elapsed))
      }, 1000)
    } else if (workTimerRef.current) {
      clearInterval(workTimerRef.current)
    }

    return () => {
      if (workTimerRef.current) {
        clearInterval(workTimerRef.current)
      }
    }
  }, [status, breakStatus, workStartTime, totalWorkTime])

  // Reset clock-in status at midnight
  useEffect(() => {
    const checkNewDay = () => {
      const now = new Date()
      if (now.getHours() === 0 && now.getMinutes() === 0 && now.getSeconds() === 0) {
        setTodayClockIn(false)
      }
    }

    const midnightCheck = setInterval(checkNewDay, 1000)
    return () => clearInterval(midnightCheck)
  }, [])

  // Clear error after 5 seconds
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => {
        setError(null)
      }, 5000)
      return () => clearTimeout(timer)
    }
  }, [error])

  const addToHistory = (action: ClockAction) => {
    const now = new Date()
    const timeString = now.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    })
    const newRecord = {
      action,
      time: timeString,
      timestamp: now.getTime(),
    }
    setHistory((prev) => [newRecord, ...prev].slice(0, 10))
    return newRecord
  }

  const handleClockIn = () => {
    if (todayClockIn) {
      setError("You have already clocked in today")
      return
    }

    const record = addToHistory("in")
    setStatus("in")
    setTodayClockIn(true)
    setWorkStartTime(Date.now())
    setTotalWorkTime(0)
    setElapsedWorkTime("00:00:00")
    setTotalBreakTime(0)
    setTotalBreakDisplay("00:00")
  }

  const handleClockOut = () => {
    if (status !== "in") {
      setError("You must clock in before clocking out")
      return
    }

    if (breakStatus === "active") {
      setError("Please end your break before clocking out")
      return
    }

    addToHistory("out")
    setStatus("out")

    // Calculate final work time
    if (workStartTime) {
      const finalWorkTime = Date.now() - workStartTime + totalWorkTime
      setElapsedWorkTime(formatWorkTime(finalWorkTime))
    }

    setWorkStartTime(null)
  }

  const handleStartBreak = () => {
    if (status !== "in") {
      setError("You must clock in before starting a break")
      return
    }

    if (breakStatus === "active") {
      setError("You are already on a break")
      return
    }

    addToHistory("break_start")
    setBreakStatus("active")
    setBreakStartTime(Date.now())

    // Store current work time when break starts
    if (workStartTime) {
      const currentWorkTime = Date.now() - workStartTime + totalWorkTime
      setTotalWorkTime(currentWorkTime)
      setWorkStartTime(null)
    }
  }

  const handleEndBreak = () => {
    if (breakStatus !== "active") {
      setError("You must start a break before ending it")
      return
    }

    // Calculate and add to total break time
    if (breakStartTime) {
      const currentBreakDuration = Date.now() - breakStartTime
      setTotalBreakTime((prev) => prev + currentBreakDuration)
      setTotalBreakDisplay(formatElapsedTime(totalBreakTime + currentBreakDuration))
    }

    addToHistory("break_end")
    setBreakStatus("inactive")
    setBreakStartTime(null)
    setBreakElapsedTime("00:00")

    // Resume work timer
    setWorkStartTime(Date.now())
  }

  const getActionLabel = (action: ClockAction): string => {
    switch (action) {
      case "in":
        return "Clocked In"
      case "out":
        return "Clocked Out"
      case "break_start":
        return "Started Break"
      case "break_end":
        return "Ended Break"
      default:
        return action
    }
  }

  const getActionIcon = (action: ClockAction) => {
    switch (action) {
      case "in":
        return <PlayCircle className="h-4 w-4 text-[#3B82F6]" />
      case "out":
        return <PauseCircle className="h-4 w-4 text-[#6B7280]" />
      case "break_start":
        return <Coffee className="h-4 w-4 text-[#14B8A6]" />
      case "break_end":
        return <Timer className="h-4 w-4 text-[#3B82F6]" />
      default:
        return <Clock className="h-4 w-4 text-[#6B7280]" />
    }
  }

  return (
    <Card className="w-full h-full overflow-hidden border border-[#E5E7EB] shadow-xl rounded-2xl bg-gradient-to-br from-[#F9FAFB] via-[#F0FDFA] to-[#E0F2FE] dark:from-[#1F2937] dark:via-[#134E4A] dark:to-[#0F172A] relative flex flex-col">
      <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#3B82F6] via-[#0EA5E9] to-[#14B8A6]"></div>

      <CardContent className="p-6 pt-8 flex-1 flex flex-col">
        <div className="flex flex-col items-center flex-1">
          {/* Clock Icon and Time Display */}
          <div className="mb-6 relative">
            <div className="relative rounded-full bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] p-[3px] shadow-lg">
              <div className="rounded-full bg-[#FFFFFF] dark:bg-[#1F2937] p-5 flex items-center justify-center">
                <div className="relative">
                  <Clock className="h-8 w-8 text-[#3B82F6] dark:text-[#3B82F6]" aria-hidden="true" />
                  <div
                    className="absolute inset-0 rounded-full border-2 border-[#3B82F6] dark:border-[#3B82F6] opacity-70"
                    style={{
                      clipPath: `polygon(50% 50%, 50% 0%, ${50 - 50 * Math.cos((2 * Math.PI * seconds) / 60)}% ${
                        50 - 50 * Math.sin((2 * Math.PI * seconds) / 60)
                      }%, 50% 50%)`,
                    }}
                    aria-hidden="true"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <h3 className="text-xl font-bold mb-1 text-[#1F2937] dark:text-[#F9FAFB]">Time Tracker</h3>
          <p className="text-sm text-[#6B7280] dark:text-[#6B7280] mb-6">Record your work hours</p>

          {/* Error Alert */}
          {error && (
            <Alert
              variant="destructive"
              className="mb-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-800 dark:text-red-300"
            >
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Current Time Display */}
          <div
            className="w-full bg-[#FFFFFF] dark:bg-[#1F2937] backdrop-blur-sm rounded-xl p-5 mb-6 text-center relative overflow-hidden shadow-md border border-[#E5E7EB] dark:border-[#E5E7EB]/20"
            role="status"
            aria-label={`Current time: ${currentTime}`}
          >
            <div className="absolute -top-6 -right-6 w-12 h-12 rounded-full bg-[#3B82F6]/10 dark:bg-[#3B82F6]/10"></div>
            <div className="absolute -bottom-6 -left-6 w-12 h-12 rounded-full bg-[#14B8A6]/10 dark:bg-[#14B8A6]/10"></div>

            <div className="flex items-center justify-center gap-2 mb-2">
              <Calendar className="h-4 w-4 text-[#3B82F6] dark:text-[#3B82F6]" aria-hidden="true" />
              <p className="text-sm text-[#6B7280] dark:text-[#6B7280] font-medium">{currentDate}</p>
            </div>

            <div className="text-3xl font-mono font-bold text-[#1F2937] dark:text-[#F9FAFB] relative">
              {currentTime}
              <div
                className="absolute -right-1 top-0 h-full w-[2px] bg-[#3B82F6] animate-pulse"
                aria-hidden="true"
              ></div>
            </div>
          </div>

          {/* Status Indicators */}
          <div className="w-full mb-5 space-y-4">
            {/* Work Status */}
            <div className="bg-[#FFFFFF] dark:bg-[#1F2937] backdrop-blur-sm rounded-xl p-4 border border-[#E5E7EB] dark:border-[#E5E7EB]/20 shadow-sm">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <div
                    className={`h-3 w-3 rounded-full ${
                      status === "in" ? "bg-[#14B8A6] animate-pulse" : "bg-[#6B7280]"
                    } mr-1`}
                    aria-hidden="true"
                  ></div>
                  <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">Work Status</span>
                </div>
                <Badge
                  variant="default"
                  className={`text-xs font-medium px-3 py-1 ${
                    status === "in"
                      ? "bg-[#14B8A6]/20 text-[#14B8A6] dark:bg-[#14B8A6]/20 dark:text-[#14B8A6] border border-[#14B8A6]/30"
                      : "bg-[#6B7280]/20 text-[#6B7280] dark:bg-[#6B7280]/20 dark:text-[#6B7280] border border-[#6B7280]/30"
                  }`}
                  role="status"
                  aria-label={`Status: ${status === "in" ? "Clocked In" : "Clocked Out"}`}
                >
                  {status === "in" ? "Clocked In" : "Clocked Out"}
                </Badge>
              </div>
              <div
                className="h-2 w-full bg-[#F9FAFB] dark:bg-[#1F2937]/50 rounded-full overflow-hidden"
                role="progressbar"
                aria-valuenow={status === "in" ? 100 : 0}
                aria-valuemin={0}
                aria-valuemax={100}
                aria-label="Clock status progress"
              >
                <div
                  className={`h-full ${
                    status === "in" ? "bg-[#14B8A6]" : "bg-[#6B7280]"
                  } rounded-full transition-all duration-500`}
                  style={{ width: status === "in" ? "100%" : "0%" }}
                ></div>
              </div>
            </div>

            {/* Break Status - Only show when clocked in */}
            {status === "in" && (
              <div className="bg-[#FFFFFF] dark:bg-[#1F2937] backdrop-blur-sm rounded-xl p-4 border border-[#E5E7EB] dark:border-[#E5E7EB]/20 shadow-sm">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <div
                      className={`h-3 w-3 rounded-full ${
                        breakStatus === "active" ? "bg-[#3B82F6] animate-pulse" : "bg-[#6B7280]"
                      } mr-1`}
                      aria-hidden="true"
                    ></div>
                    <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">Break Status</span>
                  </div>
                  <Badge
                    variant={breakStatus === "active" ? "default" : "secondary"}
                    className={`text-xs font-medium px-3 py-1 ${
                      breakStatus === "active"
                        ? "bg-[#3B82F6]/20 text-[#3B82F6] dark:bg-[#3B82F6]/20 dark:text-[#3B82F6] border border-[#3B82F6]/30"
                        : "bg-[#6B7280]/20 text-[#6B7280] dark:bg-[#6B7280]/20 dark:text-[#6B7280] border border-[#6B7280]/30"
                    }`}
                    role="status"
                    aria-label={`Break Status: ${breakStatus === "active" ? "On Break" : "Working"}`}
                  >
                    {breakStatus === "active" ? "On Break" : "Working"}
                  </Badge>
                </div>
                <div
                  className="h-2 w-full bg-[#F9FAFB] dark:bg-[#1F2937]/50 rounded-full overflow-hidden"
                  role="progressbar"
                  aria-valuenow={breakStatus === "active" ? 100 : 0}
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-label="Break status progress"
                >
                  <div
                    className={`h-full ${
                      breakStatus === "active" ? "bg-[#3B82F6]" : "bg-[#6B7280]"
                    } rounded-full transition-all duration-500`}
                    style={{ width: breakStatus === "active" ? "100%" : "0%" }}
                  ></div>
                </div>
              </div>
            )}
          </div>

          {/* Time Tracking Cards */}
          <div className="w-full space-y-3 mb-5">
            {/* Current break timer */}
            {breakStatus === "active" && (
              <div className="w-full bg-gradient-to-r from-[#3B82F6]/10 to-[#F9FAFB] dark:from-[#3B82F6]/20 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                <div className="flex items-center gap-2">
                  <Coffee className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                  <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Current Break</span>
                </div>
                <div className="font-mono font-bold text-[#3B82F6] dark:text-[#3B82F6] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  {breakElapsedTime}
                </div>
              </div>
            )}

            {/* Total break time - show when clocked in */}
            {status === "in" && (totalBreakTime > 0 || breakStatus === "active") && (
              <div className="w-full bg-gradient-to-r from-[#3B82F6]/5 to-[#F9FAFB] dark:from-[#3B82F6]/10 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                <div className="flex items-center gap-2">
                  <Coffee className="h-5 w-5 text-[#3B82F6]/70 dark:text-[#3B82F6]/70" />
                  <span className="text-sm text-[#6B7280] dark:text-[#6B7280] font-medium">Total Break Time</span>
                </div>
                <div className="font-mono font-bold text-[#3B82F6]/80 dark:text-[#3B82F6]/80 text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  {totalBreakDisplay}
                </div>
              </div>
            )}

            {/* Hours worked - show when clocked in */}
            {status === "in" && (
              <div className="w-full bg-gradient-to-r from-[#14B8A6]/10 to-[#F9FAFB] dark:from-[#14B8A6]/20 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                <div className="flex items-center gap-2">
                  <Clock className="h-5 w-5 text-[#14B8A6] dark:text-[#14B8A6]" />
                  <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Hours Worked</span>
                </div>
                <div className="font-mono font-bold text-[#14B8A6] dark:text-[#14B8A6] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  {elapsedWorkTime}
                </div>
              </div>
            )}

            {/* Total hours worked - show when clocked out but worked today */}
            {status === "out" && todayClockIn && (
              <div className="w-full bg-gradient-to-r from-[#6B7280]/10 to-[#F9FAFB] dark:from-[#6B7280]/20 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                <div className="flex items-center gap-2">
                  <Clock className="h-5 w-5 text-[#6B7280] dark:text-[#6B7280]" />
                  <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Total Hours Worked</span>
                </div>
                <div className="font-mono font-bold text-[#6B7280] dark:text-[#6B7280] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  {elapsedWorkTime}
                </div>
              </div>
            )}

            {/* Total break time - show when clocked out but had breaks today */}
            {status === "out" && todayClockIn && totalBreakTime > 0 && (
              <div className="w-full bg-gradient-to-r from-[#6B7280]/5 to-[#F9FAFB] dark:from-[#6B7280]/10 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                <div className="flex items-center gap-2">
                  <Coffee className="h-5 w-5 text-[#6B7280] dark:text-[#6B7280]" />
                  <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Total Break Time</span>
                </div>
                <div className="font-mono font-bold text-[#6B7280] dark:text-[#6B7280] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  {totalBreakDisplay}
                </div>
              </div>
            )}
          </div>

          {/* History Section */}
          {history.length > 0 && (
            <div className="w-full mb-5">
              <button
                onClick={() => setShowHistory(!showHistory)}
                className="flex items-center justify-between w-full text-sm font-medium text-[#3B82F6] dark:text-[#3B82F6] hover:text-[#2563EB] dark:hover:text-[#2563EB] transition-colors bg-[#FFFFFF] dark:bg-[#1F2937] backdrop-blur-sm rounded-xl p-4 border border-[#E5E7EB] dark:border-[#E5E7EB]/20 shadow-sm"
              >
                <div className="flex items-center gap-2">
                  <History className="h-5 w-5" />
                  <span>Recent Activity</span>
                </div>
                {showHistory ? (
                  <ChevronUp className="h-5 w-5 transition-transform duration-300" />
                ) : (
                  <ChevronDown className="h-5 w-5 transition-transform duration-300" />
                )}
              </button>

              {showHistory && (
                <div className="mt-2 space-y-2 bg-[#FFFFFF] dark:bg-[#1F2937] backdrop-blur-sm p-4 rounded-xl text-sm max-h-48 overflow-y-auto border border-[#E5E7EB] dark:border-[#E5E7EB]/20 shadow-inner">
                  {history.map((entry, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between p-2 rounded-lg hover:bg-[#F9FAFB] dark:hover:bg-[#1F2937]/80 transition-colors"
                    >
                      <div className="flex items-center gap-2">
                        {getActionIcon(entry.action)}
                        <span className="text-[#1F2937] dark:text-[#F9FAFB] font-medium">
                          {getActionLabel(entry.action)}
                        </span>
                      </div>
                      <span className="text-[#6B7280] dark:text-[#6B7280] bg-[#F9FAFB] dark:bg-[#1F2937]/50 px-2 py-0.5 rounded-md text-xs font-mono border border-[#E5E7EB] dark:border-[#E5E7EB]/10">
                        {entry.time}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="mt-auto w-full space-y-3">
            {/* Clock In/Out Button */}
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    onClick={status === "in" ? handleClockOut : handleClockIn}
                    className={`w-full rounded-xl py-6 font-medium transition-all duration-300 shadow-lg ${
                      status === "in"
                        ? "bg-gradient-to-r from-[#3B82F6] to-[#14B8A6] hover:from-[#2563EB] hover:to-[#0D9488] text-white border-0"
                        : "bg-gradient-to-r from-[#3B82F6] to-[#3B82F6] hover:from-[#2563EB] hover:to-[#2563EB] text-white border-0"
                    }`}
                    aria-label={`${
                      status === "in" ? "Clock Out" : "Clock In"
                    } - Click to ${status === "in" ? "clock out" : "clock in"}`}
                  >
                    <div className="flex items-center justify-center gap-3">
                      {status === "in" ? (
                        <XCircle className="h-6 w-6" aria-hidden="true" />
                      ) : (
                        <CheckCircle className="h-6 w-6" aria-hidden="true" />
                      )}
                      <span className="text-lg font-semibold">Clock {status === "in" ? "Out" : "In"}</span>
                    </div>
                  </Button>
                </TooltipTrigger>
                <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                  <p>{status === "in" ? "End your work day" : "Start your work day"}</p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>

            {/* Break Buttons - Only show when clocked in */}
            {status === "in" && (
              <div className="grid grid-cols-2 gap-3">
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        onClick={handleStartBreak}
                        disabled={breakStatus === "active"}
                        className={`rounded-xl py-4 font-medium transition-all duration-300 ${
                          breakStatus === "active"
                            ? "bg-[#E5E7EB] text-[#6B7280] dark:bg-[#1F2937]/70 dark:text-[#6B7280] cursor-not-allowed"
                            : "bg-[#3B82F6] hover:bg-[#2563EB] text-white shadow-md border-0"
                        }`}
                        aria-label="Start Break"
                      >
                        <div className="flex items-center justify-center gap-2">
                          <Coffee className="h-5 w-5" aria-hidden="true" />
                          <span>Start Break</span>
                        </div>
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                      <p>Take a break from work</p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>

                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        onClick={handleEndBreak}
                        disabled={breakStatus !== "active"}
                        className={`rounded-xl py-4 font-medium transition-all duration-300 ${
                          breakStatus !== "active"
                            ? "bg-[#E5E7EB] text-[#6B7280] dark:bg-[#1F2937]/70 dark:text-[#6B7280] cursor-not-allowed"
                            : "bg-[#14B8A6] hover:bg-[#0D9488] text-white shadow-md border-0"
                        }`}
                        aria-label="End Break"
                      >
                        <div className="flex items-center justify-center gap-2">
                          <Clock className="h-5 w-5" aria-hidden="true" />
                          <span>End Break</span>
                        </div>
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                      <p>Return to work</p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
