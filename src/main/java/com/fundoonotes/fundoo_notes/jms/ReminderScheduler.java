package com.fundoonotes.fundoo_notes.jms;

import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ReminderProducer reminderProducer;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkReminders() {

        System.out.println("Reminders check: " + LocalDateTime.now());

        // DIAGNOSTIC PRINT: Print all notes that have reminders in the DB
        try {
            List<Note> allReminders = noteRepository.findAll().stream()
                    .filter(n -> n.getReminder() != null)
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("DIAGNOSTIC: Total active reminders in DB: " + allReminders.size());
            for (Note n : allReminders) {
                System.out.println("DIAGNOSTIC Note ID: " + n.getId() + " | Title: " + n.getTitle() + " | Reminder in DB: " + n.getReminder() + " | IsTrashed: " + n.isTrashed());
            }
        } catch (Exception e) {
            System.out.println("DIAGNOSTIC error: " + e.getMessage());
        }

        List<Note> dueNotes = noteRepository
                .findByReminderBeforeAndIsTrashedFalse(
                        LocalDateTime.now()
                );

        if (dueNotes.isEmpty()) {
            System.out.println("No reminders.");
            return;
        }

        System.out.println("Found " + dueNotes.size()
                + " reminder(s) due!");

        for (Note note : dueNotes) {
            String recipient = note.getReminderSetBy();
            if (recipient == null || recipient.trim().isEmpty()) {
                recipient = note.getUser().getEmail();
            }

            reminderProducer.sendReminder(
                    recipient,
                    note.getTitle()
            );
            note.setReminder(null);
            note.setReminderSetBy(null);
            noteRepository.save(note);
            System.out.println("Reminder processed for " + recipient + ": "
                    + note.getTitle());
        }
    }
}