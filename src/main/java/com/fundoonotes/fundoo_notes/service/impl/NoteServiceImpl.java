package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.dto.LabelResponseDTO;
import com.fundoonotes.fundoo_notes.dto.NoteDTO;
import com.fundoonotes.fundoo_notes.dto.NoteResponseDTO;
import com.fundoonotes.fundoo_notes.dto.ReminderDTO;
import com.fundoonotes.fundoo_notes.dto.CollaboratorResponseDTO;
import com.fundoonotes.fundoo_notes.model.Collaborator;
import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.CollaboratorRepository;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import com.fundoonotes.fundoo_notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    // CONVERT NOTE TO RESPONSE DTO
    private NoteResponseDTO toDTO(Note note) {
        NoteResponseDTO dto = new NoteResponseDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setColor(note.getColor());
        dto.setPinned(note.isPinned());
        dto.setArchived(note.isArchived());
        dto.setTrashed(note.isTrashed());
        dto.setReminder(note.getReminder());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setOwnerEmail(note.getUser().getEmail()); // POPULATE OWNER EMAIL
        List<LabelResponseDTO> labelDTOs = note.getLabels()
                .stream()
                .map(label -> new LabelResponseDTO(label.getId(), label.getName()))
                .collect(Collectors.toList());
        dto.setLabels(labelDTOs);

        List<Collaborator> collaborators = collaboratorRepository.findByNote(note);
        List<CollaboratorResponseDTO> collaboratorDTOs = collaborators.stream()
                .map(c -> new CollaboratorResponseDTO(
                        c.getId(),
                        c.getUser().getEmail(),
                        c.getUser().getFirstName(),
                        c.getUser().getLastName(),
                        c.getPermission().name()
                ))
                .collect(Collectors.toList());
        dto.setCollaborators(collaboratorDTOs);

        return dto;
    }

    // CONVERT LIST OF NOTES TO RESPONSE DTOS (BULK LOAD - NO N+1 SELECTS)
    private List<NoteResponseDTO> toDTOList(List<Note> notes, String fallbackOwnerEmail) {
        if (notes == null || notes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Collaborator> allCollaborators = collaboratorRepository.findByNoteIn(notes);
        Map<Long, List<Collaborator>> collaboratorsByNoteId = allCollaborators.stream()
                .collect(Collectors.groupingBy(c -> c.getNote().getId()));

        List<NoteResponseDTO> dtos = new ArrayList<>();
        for (Note note : notes) {
            NoteResponseDTO dto = new NoteResponseDTO();
            dto.setId(note.getId());
            dto.setTitle(note.getTitle());
            dto.setContent(note.getContent());
            dto.setColor(note.getColor());
            dto.setPinned(note.isPinned());
            dto.setArchived(note.isArchived());
            dto.setTrashed(note.isTrashed());
            dto.setReminder(note.getReminder());
            dto.setCreatedAt(note.getCreatedAt());
            dto.setUpdatedAt(note.getUpdatedAt());
            dto.setOwnerEmail(fallbackOwnerEmail != null ? fallbackOwnerEmail : (note.getUser() != null ? note.getUser().getEmail() : ""));

            List<LabelResponseDTO> labelDTOs = note.getLabels()
                    .stream()
                    .map(label -> new LabelResponseDTO(label.getId(), label.getName()))
                    .collect(Collectors.toList());
            dto.setLabels(labelDTOs);

            List<Collaborator> collaborators = collaboratorsByNoteId.getOrDefault(note.getId(), new ArrayList<>());
            List<CollaboratorResponseDTO> collaboratorDTOs = collaborators.stream()
                    .map(c -> new CollaboratorResponseDTO(
                            c.getId(),
                            c.getUser().getEmail(),
                            c.getUser().getFirstName(),
                            c.getUser().getLastName(),
                            c.getPermission().name()
                    ))
                    .collect(Collectors.toList());
            dto.setCollaborators(collaboratorDTOs);

            dtos.add(dto);
        }
        return dtos;
    }

    // GET LOGGED IN USER
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET NOTE BELONGING TO USER (owner only — for destructive/organizing actions)
    private Note getNoteOfUser(Long noteId, User user) {
        return noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));
    }

    // GET NOTE EDITABLE BY USER — owner OR any collaborator
    private Note getEditableNote(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (note.getUser().getId().equals(user.getId())) {
            return note;
        }

        Collaborator collaborator = collaboratorRepository
                .findByNoteAndUser(note, user)
                .orElseThrow(() -> new RuntimeException("Note not found or you don't have permission"));

        return note;
    }

    @Override
    public NoteResponseDTO createNote(NoteDTO dto, String email) {
        User user = getUser(email);
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setColor(dto.getColor() != null ? dto.getColor() : "#FFFFFF");
        note.setUser(user);
        return toDTO(noteRepository.save(note));
    }

    @Override
    public List<NoteResponseDTO> getAllNotes(String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.findByUserAndIsTrashedFalseAndIsArchivedFalse(user);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public NoteResponseDTO updateNote(Long id, NoteDTO dto, String userEmail) {
        User user = getUser(userEmail);
        Note note = getEditableNote(id, user);

        if (dto.getTitle() != null) {
            note.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            note.setContent(dto.getContent());
        }
        if (dto.getColor() != null) {
            note.setColor(dto.getColor());
        }

        Note updatedNote = noteRepository.save(note);
        return toDTO(updatedNote);
    }

    @Override
    @Transactional
    public String deleteNote(Long noteId, String email) {
        User user = getUser(email);
        Note note = getNoteOfUser(noteId, user);
        
        // Delete collaborator relationships first to avoid FK constraint errors
        List<Collaborator> collaborators = collaboratorRepository.findByNote(note);
        collaboratorRepository.deleteAll(collaborators);

        noteRepository.delete(note);
        return "Note deleted successfully";
    }

    @Override
    public String togglePin(Long noteId, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        note.setPinned(!note.isPinned());
        noteRepository.save(note);
        return note.isPinned() ? "Note pinned" : "Note unpinned";
    }

    @Override
    public String toggleArchive(Long noteId, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        note.setArchived(!note.isArchived());
        noteRepository.save(note);
        return note.isArchived() ? "Note archived" : "Note unarchived";
    }

    @Override
    public String toggleTrash(Long noteId, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        note.setTrashed(!note.isTrashed());
        noteRepository.save(note);
        return note.isTrashed() ? "Note moved to trash" : "Note restored";
    }

    @Override
    public List<NoteResponseDTO> getPinnedNotes(String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.findByUserAndIsPinnedTrueAndIsTrashedFalse(user);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public List<NoteResponseDTO> getArchivedNotes(String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.findByUserAndIsArchivedTrueAndIsTrashedFalse(user);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public List<NoteResponseDTO> getTrashedNotes(String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.findByUserAndIsTrashedTrue(user);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public List<NoteResponseDTO> searchNotes(String keyword, String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.searchNotes(user, keyword);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public List<NoteResponseDTO> filterByColor(String color, String email) {
        User user = getUser(email);
        List<Note> notes = noteRepository.findByUserAndColorAndIsTrashedFalseAndIsArchivedFalse(user, color);
        return toDTOList(notes, user.getEmail());
    }

    @Override
    public NoteResponseDTO setReminder(Long noteId, ReminderDTO dto, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        LocalDateTime reminderTime = dto.getReminderTime();
        if (reminderTime != null) {
            reminderTime = reminderTime.withSecond(0).withNano(0);
        }
        if (reminderTime == null || reminderTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reminder time must be in the future");
        }
        note.setReminder(reminderTime);
        note.setReminderSetBy(email);
        return toDTO(noteRepository.save(note));
    }

    @Override
    public NoteResponseDTO removeReminder(Long noteId, String email) {
        User user = getUser(email);
        Note note = getEditableNote(noteId, user);
        note.setReminder(null);
        note.setReminderSetBy(null);
        return toDTO(noteRepository.save(note));
    }
}
