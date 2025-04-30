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
import { authService } from '@/lib/auth';
import { toast } from "sonner"
import { Toaster } from "@/components/ui/sonner"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

type ClockAction = "in" | "out" | "break_start" | "break_end"

type ClockRecord = {
  action: ClockAction
  time: string
  timestamp: number
}

interface AttendanceInfo {
  attendanceId: string;
  employeeId: string;
  employeeName: string;
  date: string;
  clockInTime: string | null;
  clockOutTime: string | null;
  totalHours: number | null;
  status: string;
  remarks: string | null;
  overtimeHours: number | null;
  reasonForAbsence: string | null;
  approvedByManager: boolean;
  workTimeInSched: string | null;
  workTimeOutSched: string | null;
}

interface EmployeeProfile {
  employeeId: string
  idNumber: string
  firstName: string
  lastName: string
  email: string
  gender: string
  hireDate: string
  dateOfBirth: string
  address: string
  phoneNumber: string
  maritalStatus: string
  status: boolean
  employmentStatus: string
  departmentId: string
  departmentName: string
  jobId: string
  jobName: string
  roleId: string
  roleName: string
  createdAt: string
  workTimeInSched: string
  workTimeOutSched: string
}

type BreakType = "lunch" | "morning" | "afternoon" | null;

interface BreakInfo {
  type: BreakType;
  startTime: number | null;
  elapsedTime: string;
  totalTime: number;
  displayTime: string;
}

export function ClockInOut() {
  const [employeeProfile, setEmployeeProfile] = useState<EmployeeProfile | null>(null);
  const [attendanceInfo, setAttendanceInfo] = useState<AttendanceInfo | null>(null);
  const [status, setStatus] = useState<"in" | "out">(() => {
    // Check localStorage on initial render
    if (typeof window !== 'undefined') {
      const storedStatus = localStorage.getItem('clockStatus');
      return storedStatus === 'in' ? 'in' : 'out';
    }
    return 'out';
  });
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
  const [breakInfo, setBreakInfo] = useState<BreakInfo>({
    type: null,
    startTime: null,
    elapsedTime: "00:00",
    totalTime: 0,
    displayTime: "00:00"
  });
  const [lunchBreakUsed, setLunchBreakUsed] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('lunchBreakUsed');
      return stored === 'true';
    }
    return false;
  });
  const [morningBreakUsed, setMorningBreakUsed] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('morningBreakUsed');
      return stored === 'true';
    }
    return false;
  });
  const [afternoonBreakUsed, setAfternoonBreakUsed] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('afternoonBreakUsed');
      return stored === 'true';
    }
    return false;
  });
  const [breakError, setBreakError] = useState<string | null>(null);
  const [showEndBreakDialog, setShowEndBreakDialog] = useState(false);
  const [breakTimeLimit, setBreakTimeLimit] = useState<string>("");
  const [currentBreakType, setCurrentBreakType] = useState<BreakType>(null);
  const [morningBreakTime, setMorningBreakTime] = useState<number>(0);
  const [lunchBreakTime, setLunchBreakTime] = useState<number>(0);
  const [afternoonBreakTime, setAfternoonBreakTime] = useState<number>(0);

  // Format milliseconds to MM:SS
  const formatElapsedTime = (ms: number): string => {
    const minutes = Math.floor(ms / 60000)
    const seconds = Math.floor((ms % 60000) / 1000)
    return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`
  }

  // Get break time limit in milliseconds
  const getBreakTimeLimit = (type: BreakType): number => {
    switch (type) {
      case "lunch":
        return 60 * 60 * 1000; // 1 hour
      case "morning":
      case "afternoon":
        return 15 * 60 * 1000; // 15 minutes
      default:
        return 0;
    }
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
    if (breakInfo.type && breakInfo.startTime) {
      const timeLimit = getBreakTimeLimit(breakInfo.type);
      breakTimerRef.current = setInterval(() => {
        const currentBreakTime = Date.now() - breakInfo.startTime!;
        const remainingTime = Math.max(0, timeLimit - currentBreakTime);
        
        setBreakInfo(prev => ({
          ...prev,
          elapsedTime: formatElapsedTime(remainingTime),
          displayTime: formatElapsedTime(remainingTime)
        }));

        // Check if break time is up
        if (remainingTime <= 0) {
          setBreakError(`${breakInfo.type} break time is up!`);
          handleEndBreak();
        }
      }, 1000);
    } else if (breakTimerRef.current) {
      clearInterval(breakTimerRef.current);
    }

    return () => {
      if (breakTimerRef.current) {
        clearInterval(breakTimerRef.current);
      }
    };
  }, [breakInfo.type, breakInfo.startTime]);

  // Fetch today's attendance
  useEffect(() => {
    const fetchTodayAttendance = async () => {
      try {
        const response = await fetch('/api/employee/attendance/today', {
          headers: {
            'Authorization':  `Bearer ${authService.getToken()}`,
          }
        });



        if (!response.ok) {
          if (response.status === 404) {
            setAttendanceInfo(null);
            if (localStorage.getItem('clockStatus') === 'in') {
              localStorage.removeItem('clockStatus');
              setStatus('out');
            }
            return;
          }
          throw new Error('Failed to fetch attendance');
        }
       
        const data = await response.json();
        if (data) {
          setAttendanceInfo(data);
          if (data.clockInTime && !data.clockOutTime) {
            localStorage.setItem('clockStatus', 'in');
            setStatus('in');
            setTodayClockIn(true);
            
            if (data.status === "CLOCKED_IN") {
              localStorage.removeItem('lunchBreakUsed');
              localStorage.removeItem('morningBreakUsed');
              localStorage.removeItem('afternoonBreakUsed');
              setLunchBreakUsed(false);
              setMorningBreakUsed(false);
              setAfternoonBreakUsed(false);
              setTotalBreakTime(0);
              setTotalBreakDisplay("00:00");
              setMorningBreakTime(0);
              setLunchBreakTime(0);
              setAfternoonBreakTime(0);
            }
          } else {
            localStorage.removeItem('clockStatus');
            setStatus('out');
            setTodayClockIn(false);
          }
        }
      } catch (error) {
        setAttendanceInfo(null);
      }
    };

    fetchTodayAttendance();
  }, []);

  // Work time tracking effect
  useEffect(() => {
    if (status === "in" && breakStatus === "inactive") {
      workTimerRef.current = setInterval(() => {
        if (attendanceInfo?.clockInTime) {
          // Calculate time from clock-in time to now
          const clockInDate = new Date(attendanceInfo.date + 'T' + attendanceInfo.clockInTime);
          const now = new Date();
          const elapsedMs = now.getTime() - clockInDate.getTime();
          setElapsedWorkTime(formatWorkTime(elapsedMs));
        } else if (workStartTime) {
          // Fallback to local timer if no attendance info
          const elapsed = Date.now() - workStartTime + totalWorkTime;
          setElapsedWorkTime(formatWorkTime(elapsed));
        }
      }, 1000);
    } else if (workTimerRef.current) {
      clearInterval(workTimerRef.current);
    }

    return () => {
      if (workTimerRef.current) {
        clearInterval(workTimerRef.current);
      }
    };
  }, [status, breakStatus, workStartTime, totalWorkTime, attendanceInfo]);

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

  useEffect(() => {
    const fetchEmployeeProfile = async () => {
      try {
        const profile = await authService.getEmployeeProfile();
        setEmployeeProfile(profile);
      } catch (error) {
        
      }
    };

    fetchEmployeeProfile();
  }, []);

  // Reset break states at midnight
  useEffect(() => {
    const checkNewDay = () => {
      const now = new Date();
      if (now.getHours() === 0 && now.getMinutes() === 0 && now.getSeconds() === 0) {
        setLunchBreakUsed(false);
        setMorningBreakUsed(false);
        setAfternoonBreakUsed(false);
        localStorage.removeItem('lunchBreakUsed');
        localStorage.removeItem('morningBreakUsed');
        localStorage.removeItem('afternoonBreakUsed');
      }
    };

    const midnightCheck = setInterval(checkNewDay, 1000);
    return () => clearInterval(midnightCheck);
  }, []);

  const handleClockIn = async () => {
    if (todayClockIn) {
      toast.error("You have already clocked in today", {
        description: "Please clock out first before clocking in again.",
        duration: 5000,
      });
      return;
    }

    if (!employeeProfile) {
      toast.error("Unable to fetch employee profile", {
        description: "Please try again or contact support if the issue persists.",
        duration: 5000,
      });
      return;
    }

    try {
      const response = await fetch('/api/employee/attendance/clock-in', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authService.getToken()}`
        },
        body: JSON.stringify({
          employeeId: employeeProfile.employeeId,
          remarks: null
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to clock in');
      }

      const data = await response.json();

      // Fetch today's attendance after successful clock-in
      const attendanceResponse = await fetch('/api/employee/attendance/today', {
        headers: {
          'Authorization': `Bearer ${authService.getToken()}`
        }
      });
      if (attendanceResponse.ok) {
        const attendanceData = await attendanceResponse.json();
        setAttendanceInfo(attendanceData);
      }

      setStatus("in");
      setTodayClockIn(true);
      const now = Date.now();
      setWorkStartTime(now);
      setTotalWorkTime(0);
      setElapsedWorkTime("00:00:00");
      setTotalBreakTime(0);
      setTotalBreakDisplay("00:00");
      localStorage.setItem('clockStatus', 'in');
      
      toast.success("Successfully clocked in", {
        description: "Your work day has started. Have a productive day!",
        duration: 5000,
      });

    } catch (error) {
      toast.error("Failed to clock in", {
        description: error instanceof Error ? error.message : "An unexpected error occurred",
        duration: 5000,
      });
    }
  };

  const handleClockOut = async () => {
    if (status !== "in") {
      toast.error("Cannot clock out", {
        description: "You must clock in before clocking out",
        duration: 5000,
      });
      return;
    }

    if (breakStatus === "active") {
      toast.error("Cannot clock out", {
        description: "Please end your break before clocking out",
        duration: 5000,
      });
      return;
    }

    try {
      if (!attendanceInfo?.attendanceId) {
        throw new Error('No attendance record found for today');
      }

      const response = await fetch(`/api/employee/attendance/${attendanceInfo.attendanceId}/clock-out`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authService.getToken()}`
        },
        body: JSON.stringify({
          employeeId: employeeProfile?.employeeId,
          remarks: null
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to clock out');
      }

      const data = await response.json();
      setAttendanceInfo(data);
      
      setStatus("out");
      setTodayClockIn(false);
      localStorage.removeItem('clockStatus');

      if (data.clockInTime && data.clockOutTime) {
        const clockInDate = new Date(data.date + 'T' + data.clockInTime);
        const clockOutDate = new Date(data.date + 'T' + data.clockOutTime);
        const finalWorkTime = clockOutDate.getTime() - clockInDate.getTime();
        setElapsedWorkTime(formatWorkTime(finalWorkTime));
      }

      setWorkStartTime(null);

      toast.success("Successfully clocked out", {
        description: "Your work day has ended. Have a great rest of your day!",
        duration: 5000,
      });

      const attendanceResponse = await fetch('/api/employee/attendance/today', {
        headers: {
          'Authorization': `Bearer ${authService.getToken()}`
        }
      });
      if (attendanceResponse.ok) {
        const attendanceData = await attendanceResponse.json();
        setAttendanceInfo(attendanceData);
      }
    } catch (error) {
      toast.error("Failed to clock out", {
        description: error instanceof Error ? error.message : "An unexpected error occurred",
        duration: 5000,
      });
    }
  };

  const handleStartBreak = (type: BreakType) => {
    if (status !== "in") {
      setBreakError("You must clock in before starting a break");
      return;
    }

    if (breakInfo.type) {
      setBreakError("You are already on a break");
      return;
    }

    // Check if break type has already been used
    if ((type === "lunch" && lunchBreakUsed) ||
        (type === "morning" && morningBreakUsed) ||
        (type === "afternoon" && afternoonBreakUsed)) {
      setBreakError(`You have already used your ${type} break today`);
      return;
    }

    setBreakInfo({
      type,
      startTime: Date.now(),
      elapsedTime: "00:00",
      totalTime: 0,
      displayTime: "00:00"
    });

    // Store current work time when break starts
    if (workStartTime) {
      const currentWorkTime = Date.now() - workStartTime + totalWorkTime;
      setTotalWorkTime(currentWorkTime);
      setWorkStartTime(null);
    }
  };

  const handleEndBreak = () => {
    if (!breakInfo.type) {
      setBreakError("You must start a break before ending it");
      return;
    }

    // Get remaining time
    const timeLimit = getBreakTimeLimit(breakInfo.type);
    const currentBreakTime = Date.now() - breakInfo.startTime!;
    const remainingTime = Math.max(0, timeLimit - currentBreakTime);

    // If there's remaining time, show confirmation dialog
    if (remainingTime > 0) {
      const minutes = Math.floor(timeLimit / 60000);
      const hours = Math.floor(minutes / 60);
      const remainingMinutes = minutes % 60;
      
      const timeString = hours > 0 
        ? `${hours} hour${hours > 1 ? 's' : ''}`
        : `${remainingMinutes} minute${remainingMinutes !== 1 ? 's' : ''}`;

      setBreakTimeLimit(timeString);
      setCurrentBreakType(breakInfo.type);
      setShowEndBreakDialog(true);
      return;
    }

    endBreak();
  };

  const endBreak = () => {
    // Calculate and add to total break time
    if (breakInfo.startTime) {
      const currentBreakDuration = Date.now() - breakInfo.startTime;
      const newTotalBreakTime = breakInfo.totalTime + currentBreakDuration;
      
      setBreakInfo(prev => ({
        ...prev,
        totalTime: newTotalBreakTime,
        displayTime: formatElapsedTime(newTotalBreakTime)
      }));

      // Update specific break type time
      switch (breakInfo.type) {
        case "morning":
          setMorningBreakTime(prev => prev + currentBreakDuration);
          break;
        case "lunch":
          setLunchBreakTime(prev => prev + currentBreakDuration);
          break;
        case "afternoon":
          setAfternoonBreakTime(prev => prev + currentBreakDuration);
          break;
      }

      // Update total break time
      const newCombinedBreakTime = morningBreakTime + lunchBreakTime + afternoonBreakTime + currentBreakDuration;
      setTotalBreakTime(newCombinedBreakTime);
      setTotalBreakDisplay(formatElapsedTime(newCombinedBreakTime));

      // Mark the break type as used and store in localStorage
      switch (breakInfo.type) {
        case "lunch":
          setLunchBreakUsed(true);
          localStorage.setItem('lunchBreakUsed', 'true');
          break;
        case "morning":
          setMorningBreakUsed(true);
          localStorage.setItem('morningBreakUsed', 'true');
          break;
        case "afternoon":
          setAfternoonBreakUsed(true);
          localStorage.setItem('afternoonBreakUsed', 'true');
          break;
      }
    }

    setBreakInfo({
      type: null,
      startTime: null,
      elapsedTime: "00:00",
      totalTime: breakInfo.totalTime,
      displayTime: breakInfo.displayTime
    });

    // Resume work timer
    setWorkStartTime(Date.now());
    setShowEndBreakDialog(false);
  };

  // Reset all break times at midnight
  useEffect(() => {
    const checkNewDay = () => {
      const now = new Date();
      if (now.getHours() === 0 && now.getMinutes() === 0 && now.getSeconds() === 0) {
        setTotalBreakTime(0);
        setTotalBreakDisplay("00:00");
        setMorningBreakTime(0);
        setLunchBreakTime(0);
        setAfternoonBreakTime(0);
      }
    };

    const midnightCheck = setInterval(checkNewDay, 1000);
    return () => clearInterval(midnightCheck);
  }, []);

  // Update total break time whenever individual break times change
  useEffect(() => {
    const combinedBreakTime = morningBreakTime + lunchBreakTime + afternoonBreakTime;
    setTotalBreakTime(combinedBreakTime);
    setTotalBreakDisplay(formatElapsedTime(combinedBreakTime));
  }, [morningBreakTime, lunchBreakTime, afternoonBreakTime]);

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
    <>
      <Toaster 
        position="top-right" 
        richColors 
        className="mt-24" 
        style={{
          top: "6rem",
          right: "1rem"
        }}
      />
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

              <div className="grid grid-cols-2 gap-4">
               {/* Hours worked - show when clocked in */}
               <div className={`w-full bg-gradient-to-r from-[#14B8A6]/10 to-[#F9FAFB] dark:from-[#14B8A6]/20 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20 ${status !== "in" ? 'opacity-50 cursor-not-allowed' : ''}`}>
                 <div className="flex items-center gap-2">
                   <Clock className="h-5 w-5 text-[#14B8A6] dark:text-[#14B8A6]" />
                   <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Hours Worked</span>
                 </div>
                 <div className="font-mono font-bold text-[#14B8A6] dark:text-[#14B8A6] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                   {status === "in" ? elapsedWorkTime : "00:00:00"}
                 </div>
               </div>

                {/* Current Break Timer */}
                <div className={`mt-3 w-full bg-gradient-to-r from-[#3B82F6]/10 to-[#F9FAFB] dark:from-[#3B82F6]/20 dark:to-[#1F2937] rounded-xl p-4 flex items-center justify-between shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20 ${!breakInfo.type ? 'opacity-50 cursor-not-allowed pointer-events-none' : ''}`}>
                  <div className="flex items-center gap-2">
                    <Coffee className="h-5 w-5 text-[#3B82F6] dark:text-[#3B82F6]" />
                    <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">
                      {breakInfo.type ? `Current ${breakInfo.type} Break` : 'No Active Break'}
                    </span>
                  </div>
                  <div className="font-mono font-bold text-[#3B82F6] dark:text-[#3B82F6] text-lg bg-[#FFFFFF] dark:bg-[#1F2937] px-3 py-1 rounded-lg shadow-inner border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                    {breakInfo.type ? breakInfo.elapsedTime : '00:00'}
                  </div>
                </div>
              </div>

                  {/* Break Buttons - Only show when clocked in */}
                  {status === "in" && (
                <div className="grid grid-cols-3 gap-3">
                  {/* Morning Break */}
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        {breakInfo.type === "morning" ? (
                          <Button
                            onClick={handleEndBreak}
                            className="rounded-xl py-4 font-medium transition-all duration-300 bg-[#14B8A6] hover:bg-[#0D9488] text-white shadow-md border-0"
                            aria-label="End Morning Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              <Clock className="h-5 w-5" aria-hidden="true" />
                              <span>End Morning Break</span>
                            </div>
                          </Button>
                        ) : (
                          <Button
                            onClick={() => handleStartBreak("morning")}
                            disabled={breakInfo.type !== null || morningBreakUsed}
                            className={`rounded-xl py-4 font-medium transition-all duration-300 ${
                              breakInfo.type !== null || morningBreakUsed
                                ? "bg-[#E5E7EB] text-[#6B7280] dark:bg-[#1F2937]/70 dark:text-[#6B7280] cursor-not-allowed"
                                : "bg-[#3B82F6] hover:bg-[#2563EB] text-white shadow-md border-0"
                            }`}
                            aria-label="Morning Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              {morningBreakUsed ? (
                                <CheckCircle className="h-5 w-5 text-[#6B7280]" aria-hidden="true" />
                              ) : (
                                <Coffee className="h-5 w-5" aria-hidden="true" />
                              )}
                              <span>{morningBreakUsed ? "Morning Break Used" : "Morning Break"}</span>
                            </div>
                          </Button>
                        )}
                      </TooltipTrigger>
                      <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                        <p>
                          {breakInfo.type === "morning" 
                            ? "End your morning break" 
                            : morningBreakUsed 
                              ? "Morning break already used today" 
                              : "15 minutes morning break"}
                        </p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>

                  {/* Lunch Break */}
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        {breakInfo.type === "lunch" ? (
                          <Button
                            onClick={handleEndBreak}
                            className="rounded-xl py-4 font-medium transition-all duration-300 bg-[#14B8A6] hover:bg-[#0D9488] text-white shadow-md border-0"
                            aria-label="End Lunch Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              <Clock className="h-5 w-5" aria-hidden="true" />
                              <span>End Lunch Break</span>
                            </div>
                          </Button>
                        ) : (
                          <Button
                            onClick={() => handleStartBreak("lunch")}
                            disabled={breakInfo.type !== null || lunchBreakUsed}
                            className={`rounded-xl py-4 font-medium transition-all duration-300 ${
                              breakInfo.type !== null || lunchBreakUsed
                                ? "bg-[#E5E7EB] text-[#6B7280] dark:bg-[#1F2937]/70 dark:text-[#6B7280] cursor-not-allowed"
                                : "bg-[#3B82F6] hover:bg-[#2563EB] text-white shadow-md border-0"
                            }`}
                            aria-label="Lunch Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              {lunchBreakUsed ? (
                                <CheckCircle className="h-5 w-5 text-[#6B7280]" aria-hidden="true" />
                              ) : (
                                <Coffee className="h-5 w-5" aria-hidden="true" />
                              )}
                              <span>{lunchBreakUsed ? "Lunch Break Used" : "Lunch Break"}</span>
                            </div>
                          </Button>
                        )}
                      </TooltipTrigger>
                      <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                        <p>
                          {breakInfo.type === "lunch" 
                            ? "End your lunch break" 
                            : lunchBreakUsed 
                              ? "Lunch break already used today" 
                              : "1 hour lunch break"}
                        </p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>

                  {/* Afternoon Break */}
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        {breakInfo.type === "afternoon" ? (
                          <Button
                            onClick={handleEndBreak}
                            className="rounded-xl py-4 font-medium transition-all duration-300 bg-[#14B8A6] hover:bg-[#0D9488] text-white shadow-md border-0"
                            aria-label="End Afternoon Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              <Clock className="h-5 w-5" aria-hidden="true" />
                              <span>End Afternoon Break</span>
                            </div>
                          </Button>
                        ) : (
                          <Button
                            onClick={() => handleStartBreak("afternoon")}
                            disabled={breakInfo.type !== null || afternoonBreakUsed}
                            className={`rounded-xl py-4 font-medium transition-all duration-300 ${
                              breakInfo.type !== null || afternoonBreakUsed
                                ? "bg-[#E5E7EB] text-[#6B7280] dark:bg-[#1F2937]/70 dark:text-[#6B7280] cursor-not-allowed"
                                : "bg-[#3B82F6] hover:bg-[#2563EB] text-white shadow-md border-0"
                            }`}
                            aria-label="Afternoon Break"
                          >
                            <div className="flex items-center justify-center gap-2">
                              {afternoonBreakUsed ? (
                                <CheckCircle className="h-5 w-5 text-[#6B7280]" aria-hidden="true" />
                              ) : (
                                <Coffee className="h-5 w-5" aria-hidden="true" />
                              )}
                              <span>{afternoonBreakUsed ? "Afternoon Break Used" : "Afternoon Break"}</span>
                            </div>
                          </Button>
                        )}
                      </TooltipTrigger>
                      <TooltipContent className="bg-[#1F2937] text-white border-[#6B7280]">
                        <p>
                          {breakInfo.type === "afternoon" 
                            ? "End your afternoon break" 
                            : afternoonBreakUsed 
                              ? "Afternoon break already used today" 
                              : "15 minutes afternoon break"}
                        </p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                </div>
              )}

              {/* Break Error Alert */}
              {breakError && (
                <Alert
                  variant="destructive"
                  className="mt-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-800 dark:text-red-300"
                >
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{breakError}</AlertDescription>
                </Alert>
              )}

              {/* Attendance Details - Show when clocked in */}
              {attendanceInfo && attendanceInfo.clockInTime && (
                <div className="w-full bg-gradient-to-r from-[#14B8A6]/10 to-[#F9FAFB] dark:from-[#14B8A6]/20 dark:to-[#1F2937] rounded-xl p-4 shadow-sm border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                  <div className="flex items-center gap-2 mb-3">
                    <Calendar className="h-5 w-5 text-[#14B8A6] dark:text-[#14B8A6]" />
                    <span className="text-sm text-[#1F2937] dark:text-[#F9FAFB] font-medium">Today's Attendance</span>
                  </div>
                  <div className="grid grid-cols-3 gap-4">
                    {/* Clock In Time */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Clock In</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {attendanceInfo.clockInTime || 'Not clocked in'}
                      </span>
                    </div>

                    {/* Elapsed Time */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Hours Worked</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {elapsedWorkTime}
                      </span>
                    </div>

                    {/* Break Time */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Break Time</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {totalBreakDisplay}
                      </span>
                    </div>

                    {/* Clock Out Time */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Clock Out</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {attendanceInfo.clockOutTime || 'Not clocked out'}
                      </span>
                    </div>

                    {/* Total Hours */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Total Hours</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {attendanceInfo.totalHours || '0'} hours
                      </span>
                    </div>

                    {/* Date */}
                    <div className="flex flex-col items-center p-3 bg-white dark:bg-[#1F2937]/50 rounded-lg border border-[#E5E7EB] dark:border-[#E5E7EB]/20">
                      <span className="text-xs text-[#6B7280] dark:text-[#6B7280] mb-1">Date</span>
                      <span className="text-sm font-medium text-[#1F2937] dark:text-[#F9FAFB]">
                        {new Date(attendanceInfo.date).toLocaleDateString()}
                      </span>
                    </div>
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
            </div>
          </div>
        </CardContent>
      </Card>

      {/* End Break Confirmation Dialog */}
      <Dialog open={showEndBreakDialog} onOpenChange={setShowEndBreakDialog}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>End Break Early?</DialogTitle>
            <DialogDescription>
              Your {currentBreakType} break is set for {breakTimeLimit}.
              Are you sure you want to end your break now?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="flex gap-3 sm:gap-3">
            <Button
              variant="outline"
              onClick={() => setShowEndBreakDialog(false)}
              className="w-full sm:w-auto"
            >
              Continue Break
            </Button>
            <Button
              onClick={endBreak}
              className="w-full sm:w-auto bg-[#3B82F6] hover:bg-[#2563EB] text-white"
            >
              End Break
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
